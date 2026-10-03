# Patches

> Generated from `patches-list.json` - **v1.33.7** (`main`) - **15 patches** across **8 apps** - back to [README](README.md)

---

## AndroPods (pro.vitalii.andropods)

**Supported versions:** `1.5.30`

| Patch | Details |
|---|---|
| **Play Store Fix** | Removes the Google Play installation check that blocks patched APKs at startup. |
| **Premium** | Enables Pro features, including after a purchase status refresh. |

---

## Gardenscapes (com.playrix.gardenscapes)

**Supported versions:** `experimental 9.9.0`

| Patch | Details |
|---|---|
| **Repair negative stars once** | Repairs a negative saved star balance to 2 when the garden reads it, once per installation, and skips the local unofficial-install dialog. No level completion needed. Gardenscapes 9.9.0 ARM64 only. Experimental. |

---

## Good Coffee, Great Coffee (com.tapblaze.coffeebusiness)

**Supported versions:** `1.24.0`

| Patch | Details |
|---|---|
| **Coffee startup support** | Restores version-specific Unity startup after re-signing. |
| **Google Play Games via MicroG-RE** | Routes Google Play Games sign-in and player/server authorization through MicroG-RE 7.1.0+. Requires app.revanced.android.gms. |
| **Skip rewarded ads** | Completes the requested rewarded-video flow locally using the game's callbacks. |

---

## Good Pizza, Great Pizza (com.tapblaze.pizzabusiness)

**Supported versions:** `5.57.3`

| Patch | Details |
|---|---|
| **Google Play Games via MicroG-RE** | Routes Google Play Games sign-in and player/server authorization through MicroG-RE 7.1.0+. Requires app.revanced.android.gms. |
| **Hide paid offers** | Hides real-money storefront sections and cancels Google Play checkout. Keeps in-game currency exchanges and existing purchase processing. Requires ARM64 Pizza 5.57.3. |
| **Skip rewarded ads** | Completes the game's rewarded-video flow without playing an ad, keeping the requested placement and normal reward. Includes re-signed startup support. |

---

## Google Calendar (com.google.android.calendar)

**Supported versions:** `experimental 2026.37.0-984865732-release`

| Patch | Details |
|---|---|
| **Google Calendar authentication via MicroG-RE** | Routes Calendar token requests and invalidation through MicroG-RE 7.1.1+. Requires Google Play services and the same account in Android and MicroG-RE. Experimental; device synchronization still needs verification. |
| **Stabilize schedule widget** | Rebuilds schedule widget row contents on Android 16+ to work around missing tiles, deformed tile backgrounds and recycled layout corruption. Experimental; intended for Realme UI 7. Does not reset the widget. |

---

## Instagram (com.instagram.android)

**Supported versions:** `experimental 439.0.0.37.89`

| Patch | Details |
|---|---|
| **Keep DM scroll position** | Prevents Instagram Direct from jumping to the newest message after replying to an older message. Requires Piko Add settings in the same patching run. |

---

## Mapa Turystyczna (pl.mapa_turystyczna.app)

**Supported versions:** `1.16.6`

| Patch | Details |
|---|---|
| **Hide Premium prompts** | Hides Premium offers and the menu entry. Removes the trial notification on launch. |
| **Local Premium** | Enables local Premium checks for navigation, offline map controls and ads. Does not activate a subscription on your account. Experimental; device testing needed. |

---

## TikTok (com.zhiliaoapp.musically)

**Supported versions:** `47.0.3`

| Patch | Details |
|---|---|
| **Keep TikTok DM scroll position** | Keeps your position while reading older messages, including after sending messages and replies. Compatible with kveld9 patches. |

---
