# Mapa Turystyczna

Package: `pl.mapa_turystyczna.app`  
Supported input: **1.16.6 (153)**, XAPK  
Patch: **Local Premium**, opt-in

The patch changes the local access check used by navigation, offline map controls and advertising, plus the Premium boolean emitted to the UI. It does not create an order, change account data or send a purchase/activation request. Downloading maps still depends on the server accepting the request.

The subscription screen can still show the account's real status. This is intentional: the account model also drives purchase verification and refresh handling. Forcing its active flag when there is no order makes that code dereference a null order.

## Google Play

The inspected build has no PairIP `LicenseClient` or `VMRunner`. Installer checks found in bundled libraries are not evidence of a startup license screen. There is no separate Play Store Fix for this version. If a rebuilt app shows such a screen, report the exact input version and attach the startup log.

## Validation

This patch is experimental and disabled by default. Compilation and patching checks do not replace a device test. Check launch, navigation, the offline map list and an actual map download, both signed out and signed in. Refresh the account and restart the app. Record any download error separately from UI access.

Only the two local feature checks are modified. Billing, account verification and the Premium order model remain unchanged.
