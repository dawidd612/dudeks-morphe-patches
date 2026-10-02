# Google Calendar schedule widget

**Stabilize schedule widget** is selected by default and experimental. It targets
Google Calendar `2026.37.0-984865732-release`, version code `2018314914`, supplied
as an APKM. The reported device is Realme GT7 / Realme UI 7 / Android 16. The
precise firmware build and launcher version were not supplied. Runtime behavior
is changed only on Android 16 and newer.

## Evidence and scope

The supplied screenshot shows the schedule widget with missing event tile
backgrounds. The user also reports overlapping/rearranged contents after use.
On 2026-10-02 the user reported recurrence after several days: event and task
backgrounds became long ellipses instead of rounded rectangular tiles. The
first row-replacement workaround is therefore insufficient for this device.
There are no device logs or a reproducible launcher trace. Recycling/reapplying
rows is a plausible cause, not a proven diagnosis of the Realme launcher.

The actual base APK was inspected; SHA-256:
`d7f155c7ecad7ecc5c57f2c6af15edd6a0a6444ce93b9d08c69623c18e4b1cf3`.

In this build, `ScheduleViewWidgetService` constructs `cal.aaiq`, a
`RemoteViewsService.RemoteViewsFactory`. Its `getViewAt(int)` builds the complete
row, binds native event/task/date content and fill-in click intents, then returns
through seven branches. It declares 15 view types and **already returns false
from hasStableIds()**; changing that flag is not this repair. Event tile binding
is in `cal.aaii.c`, with resource-backed ImageView backgrounds. Obfuscated names
are recorded as analysis evidence; the patch identifies the factory structurally.

## Implementation

Wrap the finished row in a small `FrameLayout` RemoteViews. Its two actions are
`removeAllViews(android.R.id.content)` followed by `addView(..., originalRow)`.
Both travel in the same returned RemoteViews, so there is no separate empty
widget update. `addStableView` is deliberately avoided. On reapply, the child
is inflated fresh instead of reusing stale drawables, visibility, or dimensions.

The launcher can keep its adapter and outer row. No periodic refresh, widget
removal, root access, hidden APIs, battery exemption, or launcher patch is used.
The month widget, calendar database, sync and notification code are untouched.
Extra per-row inflation and one layout level are the performance tradeoff. This
is not a guarantee that the launcher will preserve scroll under every update.

The original row and all native actions stay intact. Android 16 propagates the
collection-child flag to nested RemoteViews and searches descendant response
tags for collection clicks. Event, task, date and secondary-action taps still
need phone verification. Framework basis:

- [Android 16 RemoteViews source](https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android16-release/core/java/android/widget/RemoteViews.java):
  `ViewGroupActionRemove`, `ViewGroupActionAdd`, `SetOnClickResponse`,
  `SetPendingIntentTemplate.findRemoteResponseTag`.
- [Public RemoteViews API](https://developer.android.com/reference/android/widget/RemoteViews).

Initialization uses the factory's Context before R8 reuses its registers. Every
return is wrapped using only its result register. Guards reject an unexpected
entry shape, return count, service/factory relationship, or already patched APK.
The helper does not store a Context or row, resolves the added layout once, and
retains original rows with a diagnostic if the resource is unexpectedly missing.

### Tile geometry after the delayed recurrence

The inspected clean APK uses `widgetschedule_chip_background.xml`, an ImageView
with ID `agenda_item_color`, source `widget_chip_fill`, and `fitXY` scaling.
The row binder (`cal.aaii.c`) selects `widget_chip_fill` (`0x7f080454`) or
`widget_chip_outline` (`0x7f080456`) and applies the native event color with
`setColorFilter`. Both drawables use rectangular GradientDrawable geometry,
with a 12dp corner radius; the outline is 1dp. The fill also contains a ripple.
The new screenshot establishes deformation, but does **not** prove whether the
launcher, drawable state, theme handling or another component causes it.

The patch now installs Android 16+ (`drawable-v36`) versions of those same
resource names. It replaces mutable shape geometry with small, white/alpha
nine-patch images at xxxhdpi: 12dp corners and a 1dp outline. An 8px centre band
stretches with 4px of safe space on each side; fixed corners retain their geometry
when the row gets wider or taller. The fill, ripple mask, theme tint and native
color-filter call are retained. The original pre-36 XML stays unchanged. Icons,
date circles, other secondary backgrounds, month drawables and layout dimensions
are unchanged. The existing complete-row replacement still addresses stale
contents. No polling or scheduled widget resets are added.

Input checks require the inspected ImageView shape, both rectangular drawable
structures, the native radius and no alternate qualified versions. Changed or
already-patched resources are rejected before new files are written. This is an
experimental workaround for the newly observed failure, not evidence that all
OEM rendering errors have been eliminated.

Nine-patch basis: [Android drawable resources](https://developer.android.com/guide/topics/resources/drawable-resource#NinePatch)
and [resizable bitmaps](https://developer.android.com/studio/write/draw9patch).

## Validation

- `python3 scripts/test_calendar_widget_rows.py` compiles the production Java
  helper against Android fakes: repeated replacement, preserved native actions,
  independent rows, concurrent initialization, idempotence, null/missing-resource
  fallback and Android-version gating. These are action-contract tests, not a
  rendering test on Android.
- `python3 scripts/test_calendar_widget_tile_shapes.py` compiles and runs the
  actual Kotlin resource transformer (requires a JDK, `kotlinc` and Pillow).
  It checks theme tint/ripple preservation, unchanged original resources, fixed
  corners at four widths/heights, and rejection without partial writes for
  changed layout/radius, qualified variants and already-patched inputs.
- The resource transformer was run on the original supplied APK's decoded
  resources, and Apktool 2.12.1 rebuilt those resources with aapt2 successfully.
  This resource-only APK is **not** a fully Morphe-patched or device-tested APK.
- `.github/workflows/verify-calendar.yml` builds extensions, the `.mpp` bundle and
  patch metadata using Java 21 and Android SDK 36.
- `scripts/VerifyCalendarWidgetDex.java` checks the actual patched APK: one
  factory, Context initialization, all seven hooks, branch/register safety,
  extension and compiled wrapper XML. Compile/run with a dexlib2-containing JAR
  on the classpath (for example the JADX all JAR).
- Realme GT7 rendering and long-duration behavior have **not** been tested here.

## Installing and testing (PL)

1. Uzyj czystego APKM wskazanej wersji i zaznacz `Stabilize schedule widget`.
   W Morphe patch jest domyslnie zaznaczony; nie ma dodatkowego przelacznika w aplikacji.
2. Zbuduj i zainstaluj aplikacje. Zmieniony APK ma podpis Morphe, a nie Google:
   Android nie pozwoli nim nadpisac aplikacji podpisanej przez Google. Sam patch
   nie omija tej kontroli. Jezeli system blokuje instalacje, zachowaj dane
   niesynchronizowane i ustal sposob instalacji przed usuwaniem aplikacji.
   Zmiana/ponowna instalacja moze wymagac jednorazowego dodania widgetu.
3. Sprawdz kolorowe kafelki, wydarzenia calodniowe/wielodniowe, zadania, godziny
   oraz klikniecia wydarzenia, zadania, daty, przycisku `+` i Meet/Chat, jesli wystepuja.
4. Przewin liste tam i z powrotem, zmien rozmiar widgetu, przelacz motyw jasny/ciemny,
   edytuj i zsynchronizuj wydarzenie, zablokuj/odblokuj ekran i uruchom telefon ponownie.
   Przetestuj tez dwa widgety o roznych rozmiarach.
5. Korzystaj przez co najmniej 7 dni, bo blad wrocil dopiero po kilku dniach.
   Sprawdz wyglad po instalacji oraz w dniach 1, 3 i 7: wypelnienie, narozniki,
   obramowania, kafelki zadan i kolory w jasnym/ciemnym motywie. Uwzglednij
   synchronizacje, zmiane daty, przewijanie, resize i restart launchera/telefonu.
   Jednorazowy poprawny wyglad nie potwierdza skutecznosci. Jezeli blad wroci,
   podaj numer firmware i launchera oraz kroki/screen. Patch jest obejściem
   konkretnej sciezki renderowania, nie potwierdzona naprawa wszystkich bledow OEM.

Do not publish the supplied APK or screenshot in this repository.
