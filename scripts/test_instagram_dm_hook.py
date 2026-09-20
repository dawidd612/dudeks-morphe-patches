#!/usr/bin/env python3
"""Host-side behavior tests of the production hook, with tiny Android/Piko fakes.

Requires a JDK, not Android SDK, network access or an Instagram account.
This tests decisions/settings integration, not Android rendering or ART verification.
"""
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
SOURCES = {
    "android/preference/Preference.java": """
package android.preference;
public class Preference { public String key, title, summary; }
""",
    "android/preference/PreferenceScreen.java": """
package android.preference;
public class PreferenceScreen {
    public Preference preference;
    public int additions;
    public Preference findPreference(String key) {
        return preference != null && key.equals(preference.key) ? preference : null;
    }
    public boolean addPreference(Preference p) { preference = p; additions++; return true; }
}
""",
    "app/morphe/extension/crimera/settings/BooleanSetting.java": """
package app.morphe.extension.crimera.settings;
public class BooleanSetting {
    public final String key;
    public final Boolean defaultValue;
    public BooleanSetting(String key, Boolean value) { this.key = key; defaultValue = value; }
}
""",
    "app/morphe/extension/crimera/sharedPreference/SharedPref.java": """
package app.morphe.extension.crimera.sharedPreference;
import app.morphe.extension.crimera.settings.BooleanSetting;
public class SharedPref {
    public static Boolean value;
    public static boolean unavailable;
    public static Boolean getBooleanPref(BooleanSetting setting) {
        if (unavailable) throw new IllegalStateException("preferences unavailable");
        return value == null ? setting.defaultValue : value;
    }
}
""",
    "app/morphe/extension/instagram/settings/preference/Helper.java": """
package app.morphe.extension.instagram.settings.preference;
import android.preference.Preference;
import app.morphe.extension.crimera.settings.BooleanSetting;
public class Helper {
    public Preference switchPreference(String title, String summary, BooleanSetting setting) {
        Preference p = new Preference();
        p.key = setting.key; p.title = title; p.summary = summary; return p;
    }
}
""",
    "HookTest.java": """
import android.preference.PreferenceScreen;
import app.morphe.extension.crimera.sharedPreference.SharedPref;
import app.morphe.extension.instagram.settings.preference.Helper;
import pl.dudek.extension.instagram.KeepDmScrollPosition;
public class HookTest {
    static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        Object reply = new Object();
        check(!KeepDmScrollPosition.shouldKeepPosition(reply, false), "default must be off");
        for (boolean enabled : new boolean[] {false, true}) {
            SharedPref.value = enabled;
            check(!KeepDmScrollPosition.shouldKeepPosition(null, false), "plain message in history");
            check(!KeepDmScrollPosition.shouldKeepPosition(null, true), "plain message at latest");
            check(!KeepDmScrollPosition.shouldKeepPosition(reply, true), "reply at latest");
            check(KeepDmScrollPosition.shouldKeepPosition(reply, false) == enabled, "old reply toggle");
        }
        SharedPref.unavailable = true;
        check(!KeepDmScrollPosition.shouldKeepPosition(reply, false), "preference failure fallback");
        PreferenceScreen screen = new PreferenceScreen();
        Helper helper = new Helper();
        KeepDmScrollPosition.addPreference(null, helper);
        KeepDmScrollPosition.addPreference(screen, null);
        check(screen.additions == 0, "null settings input");
        KeepDmScrollPosition.addPreference(screen, helper);
        KeepDmScrollPosition.addPreference(screen, helper);
        check(screen.additions == 1, "duplicate preference");
        check(screen.preference.key.equals("dudeks_keep_dm_scroll_position"), "preference key");
        check(screen.preference.title.equals("Keep scroll position when replying"), "preference title");
        System.out.println("PASS: default, eight send decisions, preference failure, null UI and duplicate UI");
    }
}
""",
}

with tempfile.TemporaryDirectory(prefix="dudeks-instagram-hook-") as temp:
    folder = Path(temp)
    files = []
    for name, source in SOURCES.items():
        path = folder / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(source)
        files.append(str(path))
    production = ROOT / "extensions/instagram/src/main/java/pl/dudek/extension/instagram/KeepDmScrollPosition.java"
    subprocess.run(["javac", "-d", str(folder), *files, str(production)], check=True)
    subprocess.run(["java", "-cp", str(folder), "HookTest"], check=True)
