package pl.dudek.extension.instagram;

import android.preference.PreferenceScreen;
import app.morphe.extension.crimera.settings.BooleanSetting;
import app.morphe.extension.crimera.sharedPreference.SharedPref;
import app.morphe.extension.instagram.settings.preference.Helper;

/** Piko add-on: no message contents, identifiers, views or thread state are retained. */
public final class KeepDmScrollPosition {
    private static final String KEY = "dudeks_keep_dm_scroll_position";
    private static final BooleanSetting ENABLED = new BooleanSetting(KEY, false);

    private KeepDmScrollPosition() {}

    public static boolean shouldKeepPosition(Object repliedMessage, boolean atLatest) {
        if (repliedMessage == null || atLatest) return false;
        try {
            return Boolean.TRUE.equals(SharedPref.getBooleanPref(ENABLED));
        } catch (RuntimeException unavailablePreferences) {
            // Preserve Instagram's behavior if preferences are unavailable during teardown.
            return false;
        }
    }

    public static void addPreference(PreferenceScreen screen, Helper helper) {
        if (screen == null || helper == null || screen.findPreference(KEY) != null) return;
        screen.addPreference(helper.switchPreference(
                "Keep scroll position when replying",
                "Stay at the current position in a conversation after replying to an older message.",
                ENABLED));
    }
}
