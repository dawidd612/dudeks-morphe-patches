# Good Pizza, Great Pizza: rewarded videos

**Skip rewarded ads** targets `com.tapblaze.pizzabusiness` **5.57.3 (2277)**,
distributed as APKM. It is selected by default and marked **experimental**.
Static analysis used the supplied base APK and ARM64 native library. No APK,
game assets, save files or user screenshots belong in this repository.

## Behavior

When the player accepts an eligible rewarded-video offer, send its placement to
the game's normal reward callback without displaying the video. Native game code
still decides the reward type, quantity, limits and eligibility. This covers the
shared video gateway, including sponsor rewards, extra tips, double rewards,
oven speed-up, undo refresh, garden/avatar items and event-specific video offers.
It does not remove purchase requirements or make unavailable game offers appear.
Tapjoy offerwall tasks and ordinary interstitial ads are separate mechanisms.

## Why this hook

The native game has one Java lookup of `showRewardedVideo(String)`. Its direct
callers and the shared `GameManager::showAd` route converge on
`BaseIronSourceWrapper.showRewardedVideo`. The existing request Runnable captures
the placement, so no placement list or hardcoded reward amount is needed.

- Keep that request and its constructor; dispatch it through
  `Cocos2dxActivity.runOnGLThread` instead of `Activity.runOnUiThread`.
- Replace only its `run` body with `onVideoStarted`, `onVideoWatched(placement)`,
  `onVideoEnded`, then `onVideoReady`. Preserve the original null-placement
  fallback, `DefaultRewardedVideo`.
- Report video readiness independently of LevelPlay's cache/no-fill result.
- Silence the original rewarded SDK's ready Runnable, so background loads cannot
  reuse the native ready timer key ahead of the local reward/end sequence.
- Before the original `Initialize` body, queue `onInitialized` and `onVideoReady`
  on the game thread. Native ready/show gates check the initialization flag
  before calling Java; changing Java readiness alone cannot bypass failed SDK
  initialization. Keep the original initialization for interstitials and Tapjoy.

The native watched callback marks the request successful, dispatches the placement
to the active game listener and updates watched counters. The ended callback
reads that success flag. Native callbacks use distinct Cocos `scheduleOnce` keys
with a 0.1-second delay; this build appends timers and processes them in insertion
order. One Java task queues the sequence, but rewards are processed later by the
native scheduler. This is not a database transaction or a new concurrent-ad queue.
The game's single-active-request behavior, daily limits, low-RAM and ANR guards
are retained. Rapid repeated taps and interrupted activity lifecycles need device
testing.

The patch does not synthesize ad-network impressions, clicks, revenue or
verification postbacks. Original game analytics/counters and SDK initialization
remain; unused ads may still load in the background.

## Re-signed startup companion

The input uses PairIP. Its Java signature check rejects a Morphe signing
certificate before game startup. The patch includes a narrowly guarded companion
that no-ops `SignatureCheck.verifyIntegrity(Context)`, `StartupLauncher.launch()`
and `LicenseClient.checkLicense(Context)` for this exact input layout.

It preserves `VMRunner`, the native loader, protected assets and all 20 protected
SDK/WorkManager callers. Some protected methods return values that are unboxed;
the broad AndroPods helper is unsuitable here. **It is not established whether
later VM calls require state from the skipped startup program.** Re-signed launch,
background/foreground transitions and gameplay must be tested on the device before
claiming working startup support. Android's installation signature rules and
server-side checks are not changed.

## Input and validation

- Base APK SHA-256:
  `b7fa170de51747f740d69dc066b5aa61ec040661a0477f0de7d86cc46623d01a`.
- ARM64 `libcocos2dcpp.so` SHA-256:
  `8566506cbfe3d2e420c9b68a0a1752d9c3023ce3bde86db26ae4d9bab52d9521`.
- The patch guards method descriptors, native callbacks, request ownership,
  register counts, expected calls and the startup program ID. Already modified
  or unexpected inputs are rejected.
- CI builds the Android patch bundle and compatibility metadata.
- `scripts/VerifyPizzaRewardDex.java` checks the actual patched DEX and executes
  its replacement request instructions against callback fakes. Run it with a
  dexlib2-containing JAR, such as the JADX all JAR, on the classpath; arguments are
  `patched.apk [original.apk]`. This does not execute the native reward handlers
  or establish that the re-signed game starts.

## Test on the phone

1. Preserve your save/sync before changing installations. Use the clean APKM
   5.57.3 (2277), retaining its native and asset splits, and select this patch.
2. Test launch, a background/foreground cycle and a restart first. A startup crash
   requires its log; do not treat successful patching as successful installation.
3. Accept each available video offer. Check that no video opens, exactly the
   normal reward appears, and its UI returns to gameplay. Check another offer,
   fast repeated taps and leaving/re-entering the game during completion.
4. Test with no ad fill/network access where the game itself allows the offer.
   Game eligibility/online requirements still apply.

Realme GT7 / Realme UI 7 runtime and every event offer have **not** been tested
here. Exact version support is based on inspected code, not a device guarantee.
