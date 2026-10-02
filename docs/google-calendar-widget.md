# Google Calendar schedule widget

`Stabilize schedule widget` is experimental and selected by default. It supports
Google Calendar `2026.37.0-984865732-release` (version code `2018314914`, APKM),
package `com.google.android.calendar`. Runtime changes apply on Android 16+.

## Complete row replacement

The schedule RemoteViews factory builds a complete row with native content and
fill-in click intents. Each finished row is wrapped in a FrameLayout whose
RemoteViews removes its old child and adds the complete row in one transaction.
This avoids reusing stale child visibility, dimensions and drawable instances.
The adapter and native actions are retained; no empty intermediate widget update,
polling, scheduled reset or `addStableView` is introduced.

The patch identifies the factory structurally and verifies its relationship to
ScheduleViewWidgetService. All seven return paths are wrapped, including shared
returns. Initialization uses the factory Context before registers are reused.
Changed bytecode shapes and already-patched inputs are rejected. The helper does
not retain a Context or row and falls back to native rows if its resource is absent.

## Stable tile geometry

The supported build uses `widgetschedule_chip_background.xml`: an ImageView with
ID `agenda_item_color`, source `widget_chip_fill`, and `fitXY` scaling. Native
binding chooses `widget_chip_fill` or `widget_chip_outline` and applies event
color with `setColorFilter`. Both resources are rectangles with 12dp corners;
the outline has a 1dp stroke and the fill includes a ripple.

Android 16+ drawable overrides retain those resource names and replace their
mutable shape geometry with white/alpha nine-patch images. The xxxhdpi pixels
preserve 12dp corners and a 1dp outline. An 8px centre band stretches, with 4px
of safe space on either side. The native theme tint, color-filter call, ripple
structure and mask are retained. Original pre-36 XML, icon/date circles, month
widget resources, layout dimensions and other backgrounds remain unchanged.

Input checks require the expected ImageView, rectangular resource structures,
12dp radius and no alternative qualified drawables. Equivalent decoder spellings
(`dp`/`dip`, numeric formatting, inline values and dimension references) are
resolved before comparison; different radii, unsupported units and alias cycles
are rejected with the offending value in the diagnostic. Changed or already-patched
resources are rejected before adding new files.

This is an experimental workaround for missing backgrounds and tile deformation.
Resource inspection and build checks do not establish the runtime cause or prove
that every launcher rendering error is eliminated. Extra row inflation is the
performance tradeoff; scroll retention under every update is not guaranteed.

Framework references:

- [Android 16 RemoteViews source](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android16-release/core/java/android/widget/RemoteViews.java)
- [RemoteViews API](https://developer.android.com/reference/android/widget/RemoteViews)
- [Nine-patch resources](https://developer.android.com/guide/topics/resources/drawable-resource#NinePatch)
- [Resizable bitmaps](https://developer.android.com/studio/write/draw9patch)

## Validation

- `python3 scripts/test_calendar_widget_rows.py`: production Java helper action
  contracts, native payload preservation, repeated replacement, concurrent
  initialization, null/missing-resource fallback and Android-version gating.
- `python3 scripts/test_calendar_widget_tile_shapes.py`: compiles and runs the
  production Kotlin transformer; checks fixed corners at four widths/heights,
  theme/ripple preservation, original resources and rejection without partial
  writes for changed layout/radius, variants and patched inputs. Requires a JDK,
  `kotlinc` (or `KOTLINC` path) and Pillow.
- The Kotlin transformer compiled, resource contracts and row contracts passed,
  and aapt2 rebuilt the supported APK resources with Apktool 2.12.1. That resource
  rebuild is not a fully Morphe-patched or device-tested APK.
- Calendar CI builds the extensions, patch bundle and metadata.
- `scripts/VerifyCalendarWidgetDex.java` verifies entry Context initialization,
  all seven hooks, branch/register safety and wrapper resource in a patched APK.
- Real-device rendering and long-duration behavior remain unverified.

## Installation and device checks (PL)

1. Uzyj czystego APKM podanej wersji i zaznacz `Stabilize schedule widget`.
   Patch jest domyslnie zaznaczony; nie ma przelacznika w aplikacji.
2. Zbuduj i zainstaluj aplikacje. Zmieniony APK ma podpis Morphe, a nie Google.
   Android nie pozwala nim nadpisac aplikacji podpisanej przez Google. Zachowaj
   niesynchronizowane dane przed zmiana instalacji. Moze byc konieczne jednorazowe
   dodanie widgetu; sam patch nie wymaga jego regularnego resetowania.
3. Sprawdz wydarzenia calodniowe, wielodniowe i z godzinami, zadania, kolory,
   obramowania oraz klikniecia wydarzenia, zadania, daty, `+`, Meet/Chat.
4. Sprawdz przewijanie, resize, dwa widgety o roznych rozmiarach, motyw jasny/ciemny,
   synchronizacje, zmiane daty, blokowanie ekranu i restart launchera/telefonu.
5. Korzystaj przez co najmniej 7 dni, aby sprawdzic stabilnosc dlugotrwalego uzycia.
   Ocen wyglad po instalacji i w dniach 1, 3 i 7. Jednorazowy poprawny wyglad ani
   brak crasha nie potwierdzaja skutecznosci.

Do not commit APKs, screenshots, device logs or account/calendar data.
