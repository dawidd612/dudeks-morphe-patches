# Google Calendar schedule widget

**Stabilize schedule widget** is selected by default and experimental. It targets
Google Calendar `2026.37.0-984865732-release`, version code `2018314914`, supplied
as an APKM. The reported device is Realme GT7 / Realme UI 7 / Android 16. The
precise firmware build and launcher version were not supplied. Runtime behavior
is changed only on Android 16 and newer.

## Evidence and scope

The supplied screenshot shows the schedule widget with missing event tile
backgrounds. The user also reports overlapping/rearranged contents after use.
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

## Validation

- `python3 scripts/test_calendar_widget_rows.py` compiles the production Java
  helper against Android fakes: repeated replacement, preserved native actions,
  independent rows, concurrent initialization, idempotence, null/missing-resource
  fallback and Android-version gating. These are action-contract tests, not a
  rendering test on Android.
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
5. Korzystaj przez co najmniej dobe, bo blad zgloszono jako nawrotowy. Jezeli wroci,
   podaj numer firmware i launchera oraz kroki/screen. Patch jest obejściem
   konkretnej sciezki renderowania, nie potwierdzona naprawa wszystkich bledow OEM.

Do not publish the supplied APK or screenshot in this repository.
