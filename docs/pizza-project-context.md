# Pizza: persistent project context

Working agreement: see the root `AGENTS.md` (authoritative instructions).

## Latest scope: hide paid offers — 2026-09-28

The user accepted the alternative of hiding broken real-money offers and
cancelling checkout, preserving in-game rewards/exchanges and their save. This
supersedes the earlier request to implement genuine checkout; no paid goods are
granted. `HidePaidOffersPatch.kt` adds a default-selected, optional ARM64 5.57.3
patch. Native changes select the existing FUNDS storefront and hide the separate
starter promotion. The public Java purchase entry queues the game's original
cancellation Runnable on the GL thread. All assets, balances/ownership code and
the other 2165 executable billing methods remain unchanged.

Candidate `artifacts/hide-paid-20260928/pizza-hidden.apk` SHA-256:
`4baed6a374788afb3be919b74b08169eeb910440c7a76e842fe2ba4b82974da7`.
It was built from source and a clean APKM, then installed with the existing key.
See [validation and failures](pizza-hidden-offers-validation.md) and
[exact native edits](pizza-store-visibility.json). Build, emitted-DEX, native,
all-split integrity and 11 asset regressions pass. Runtime tests verify three
genuine cancellations (no success/Billing), ordinary gem-to-funds exchange,
exact sponsor reward, persistence and clean setup/tutorial.

User AVD 5554 remains logged in, Chapter 2 / Day 43, cash 361.24, gems 36;
same-key update and two force-stop launches preserve all three RGB menu regions
and genuine Games sign-in success. Diagnostic AVD 5556 is clean-installed Day 1,
now closed to reduce emulator load; its earlier Day 2 save is archived in ignored
`debug-day2-backup.tar`. MicroG remains enabled. Do not reset user AVD 5554.

A diagnostic early-boot test encountered System UI ANR and a black renderer after
MicroG cancellation; force-stop/relaunch recovered. Preserve this failure and do
not claim every reboot/environment passed. Account-AVD reboot tests run with the
other AVD closed and boot services settled. Both account-AVD reboots pass:
genuine Games sign-in, identical RGB save/currency regions and no new app crash.
User AVD is left running at the preserved main menu. The feature is ready for
the authorized commit/release; publication details follow after verification.

Original-context checkpoints below remain historical evidence.

## Latest user confirmation and billing investigation — 2026-09-28

Published [v1.31.1](https://github.com/dawidd612/dudeks-morphe-patches/releases/tag/v1.31.1):
implementation commit `de4d708`, automated release commit `bf05114`.
GitHub release workflow `36443560520` succeeded. Downloaded release bundle
SHA-256: `72880b7ee51630f47640730307ddf4174d389115d468ce15c1885b8923eee639`.
Checksum, GitHub build attestation, metadata version and all three bootstrap
resources were verified. The release explicitly documents the unresolved paid
billing limitation. This publication state supersedes the older no-commit/no-release
notes below. No game APK, account data, save or signing key was published.

The user explicitly confirmed working Google Play Games, TapBlaze, correct save
and gameplay. They requested normal paid purchases, a commit and a GitHub release.
The existing candidate and its prior runtime tests remain the application baseline.
See [billing investigation](pizza-billing.md) for the newly reproduced Play Store
refusal and the publisher signing/distribution requirement. Billing is unresolved;
do not claim that releasing a Morphe bundle enables normal paid checkout.

No application code changed during the billing investigation. The verifier now
also proves parity of 2166 executable billing methods. The current user save is
Chapter 2 / Day 43 / cash 361.24 / gems 36 (the user played after the older tests).
The account AVD now runs with `-gpu swiftshader` after host-GPU black store output;
the same APK opens the shop and the stock checkout with this renderer. User data
and accounts have not been cleared or reinstalled. Debug AVD was closed cleanly.
Current billing evidence is under ignored `artifacts/billing-20260928`.

## Validated application checkpoint — 2026-09-28, 14:46 UTC

The final candidate is `artifacts/session-20260928/pizza-accepted-activity.apk`
(SHA-256 `21c0e913150194d710152fc16db807893fed099fa062a11e2ff0eb99a1bf7b63`).
The bundle is `patches/build/libs/patches-1.31.0.mpp`
(SHA-256 `1f76dbc6ac67ddcdfaeb9da20e046d4377030d53d1f446fa7e05e4be279c38ec`).
Both emulators have this APK. The completed validation and its limits are in
[runtime validation](pizza-runtime-validation.md), which supersedes the older
chronological checkpoints below. Source changes remain local and uncommitted.

- Source build, clean APKM patch, emitted DEX, all split assets, native parity and
  eight asset regressions pass. The final Games initialization runs only after
  the existing root-Activity check accepts the window and before onResume.
- Real Google authorization/player retrieval passes update, two force-stops,
  two full user-AVD reboots and network recovery after renewed MicroG consent.
  User save remains Chapter2/Day43/cash356.24/gems21, with exact image-region
  matches. TapBlaze restore was explicitly confirmed by the user; Google cloud
  save restoration is a separate, unverified capability.
- Final clean uninstall/install on debug passes age setup, first real Games
  resolution/cancellation and a rewarded offer with repeated tap and Home/resume.
  Preceding receiver candidate completed a real gameplay day and three native
  reward offers. Final candidate reads that unchanged Day2 save, including after
  verified local backup/restore; see report for exact per-candidate coverage.
- Tapjoy offline Activity-recreation test passes again on final APK, same
  PID11915, two Activity creations, no receiver leak/fatal exception. Rejected
  Activity regression has old/new evidence and two additional passing repeats.
- One earlier black-renderer symptom was not deterministic. The reproduced
  rejected-window SDK initialization was fixed; final cancellation and orphan
  window retests did not reproduce black output. Preserve the original evidence
  and retain this scenario in future regressions; do not overstate causality.
- Debug MicroG is enabled, font_scale1.0, airplaneoff, always_finish absent.
  Debug Frida server stopped. User5554 has never been cleared/reset/uninstalled;
  final PID6008 authenticated after network recovery. Retain its AVD and signing
  key. Debug Day2 backup stays in ignored artifacts; no user-account backup was
  extracted for these tests.
- ARM32/physical-device compatibility, Google cloud restore and every seasonal
  reward remain unverified. Keep experimental metadata. No push, PR or release
  has been made. Do not restart old investigation steps merely because older
  checkpoints below describe them as pending.

## Objective

Stabilize the current Morphe patch for Good Pizza, Great Pizza with genuine Google / Google Play Games authentication and preserved user progress. Work iteratively through build, patch, ADB install, runtime diagnosis and full regression retests until results are repeatable.

## Environment discovered on 2026-09-28

- Repository: https://github.com/dawidd612/dudeks-morphe-patches, cloned into `C:\Users\Komputer\Documents\Morphe` from main at `08d1e62` (v1.31.0).
- Clean input: Pizza 5.57.3 (2277) APKM in the user's Downloads folder.
- Android SDK: `%LOCALAPPDATA%\Android\Sdk`; Android Studio and Java 21 are installed.
- ADB initially reported no connected devices. Existing `Medium_Phone_API_36.1` AVD uses a Google Play x86_64 image. Existing AVD data must be preserved.
- Current patch has shared PairIP/licensing startup changes and optional MicroG-RE Games routing. Upstream notes explicitly leave runtime authentication and save restoration unverified.

## Reproducible findings (2026-09-28)

- Local Gradle initially failed resolving `app.morphe.patches:1.3.3` with GitHub Packages HTTP 401. The user authorized `read:packages` through GitHub CLI on 2026-09-28. `:patches:buildAndroid --no-daemon` now passes (158 tasks, 1m26s; `build-authorized.log`). Never print or commit the token.
- CLI 1.17.0 and published bundle 1.31.0 were downloaded and their SHA-256 checksums verified. Both Pizza patches applied to the supplied APKM in FULL mode. This is a published-bundle baseline, **not a successful local Gradle build**.
- Installed SDK command-line tools and two API 35 images. Created disposable `Morphe_Pizza_API35` (Play image, ADB `emulator-5554`) and `Morphe_Pizza_Debug_API35` (Google APIs/debug image, `emulator-5556`). Both translate ARM64 through `libndk_translation.so`. Existing AVDs were not modified. Test-package uninstallations were confined to these new AVDs, before any real login or user progress.
- Baseline fails repeatedly with `NullPointerException` in `wc.e.b`, on `GoogleApiHandler`. Decompiled code dereferences `com.mbridge.msdk.config.component.common.network.connect.okhttp.Dv.jHNdC.HrEiSdORdADDM`, which remains null when startup is disabled.
- Original signed APK splits initialize correctly and reach the licensing/Play redirect instead. Its native startup thus executes on this emulator; the emulator alone does not explain every patched-app crash.
- There are 32 methodless string-holder classes with 1459 uninitialized static strings. Read-only instrumentation of the **original signed app** recovered all 1459 non-null values. Two independent starts produced identical dumps. The failing GMS field is exactly `com.google.android.gms`.
- A diagnostic APK restoring only the original `StartupLauncher` crashes natively (`SIGSEGV`, `libpairipcore.so + 0x37ca0`, native bridge free). Do not ship this as a fix.
- A second diagnostic APK initializes all 1459 fields from the original dump while retaining the current startup/licensing edits. This removes the first null failure and allows Firebase initialization, but it still crashes later in PairIP (`libpairipcore.so + 0x8f748`, native bridge malloc). This is **partial diagnosis, not a working candidate**. Investigate remaining VM state/callers; do not no-op value-returning SDK methods or fabricate authentication results.
- User completed Google account sign-in in MicroG on `Morphe_Pizza_API35` (`emulator-5554`). Physical keyboard input was enabled with `hw.keyboard=yes`, preserving AVD data. Do not reset this emulator, clear MicroG, or uninstall the user's application data. Actual Pizza/Play Games authentication, cloud-save restoration, gameplay and stability are still unverified.
- Native caller `HKU28jIt1punH16H` is invoked by the cocos library's `DT_INIT`, outside the 20 Java callers covered by the old verifier. Skipping it without restoring its effects crashes. Two original signed-app runs recovered identical 50864 changed executable bytes and 3794 data qwords; 200 import destinations were resolved to ordinary ELF relocations, including libc's `memmove` IFUNC. `reconstruct-native.py` recreates those effects and removes only the original native `DT_INIT`, preserving C++ `DT_INIT_ARRAY`. Diagnostic `pizza-native.apk` reaches the age configuration screen and stays alive for six minutes, then crashes on SDK initialization. This is not a stable application.
- Frida located the next crash in Fyber 8.4.6 `IAMraidKit.onReceive`, a protected VM method. The exact published Maven SDK was downloaded with checksum verification. Diagnostic `pizza-fyber.apk` restores three protected Fyber receiver methods from that SDK, preserving their behavior. A later native PairIP crash remains under investigation; `trace-protected.py` logs VM entry points with ART deoptimization. No reconstructed source fix is production-ready yet.

## Durable tooling changes

- `scripts/capture_pizza_runtime.py`: captures local ADB/logcat/crash buffer/dumpsys/UIAutomator evidence, including process state before and after capture. It does not claim functional success, clear data or take screenshots.
- `scripts/VerifyPizzaIntegrationDex.java`: permits removal only of the five known distribution/split metadata markers stripped by APKM merging; all game/auth metadata still must match. The original test falsely failed on `com.android.stamp.source`.
- `scripts/verify_pizza_assets.py`: compares native libraries/assets from **every input APKM split**, rather than only base.apk. The actual baseline preserves 34 libraries and 6653 assets from 35 APKs.
- Four black-box verifier regression cases pass (preservation, changed library, missing asset split, conflicting splits); included in Pizza CI.
- Baseline emitted-DEX verifiers pass five reward-placement scenarios and preserve 1205 authentication/SDK methods including 20 protected callers. These checks demonstrably do not establish runtime viability.

## Local artifacts and resume point

- `artifacts/session-20260928/`: build failures, patch report/log, input APK splits, manifests, verifier results, installation logs, process logs, diagnostic APKs and diagnostic Java/Python/Frida sources.
- `artifacts/pizza-runtime/`: timestamped snapshots for each observed runtime failure/control.
- `artifacts/pizza-test.keystore`: same signing key used for all patched emulator candidates. Do not replace it or use it to overwrite a user's differently signed installation.
- `original-string-values-first.json` and `original-string-values.json`: two matching original runtime snapshots; `string-values.tsv`: local machine-readable encoding. These remain ignored test evidence, not a published patch resource.
- `pizza-baseline.apk`: published patches; `pizza-restored.apk`: original startup restored; `pizza-strings.apk`: original string constants restored. All are currently unsuitable for production.
- `trace-vm.py` / `trace-vm-agent.js` instrument VM entry points in the diagnostic emulator; inspect associated logs before proceeding. Frida server runs only on the disposable debug AVD.
- codebase-memory `auto_watch=false` was set. MCP later returned `Transport closed` twice; CLI `list_projects` and `index_status` succeeded. Use the installed binary's `cli --json` interface for graph-first discovery in this session.

Next: diagnose the remaining crash after Fyber restoration, integrate the proven initialization repairs into source/resources, build the updated bundle, patch the clean input and repeat the complete runtime/account/data matrix. Keep user-account emulator 5554 intact and use 5556 for destructive diagnostics. Preserve failed variants and logs; do not promote an APK based on static checks or an idle configuration screen.

## Latest checkpoint: 2026-09-28, 12:35 UTC

- The fourth failing receiver was Ad Quality 9.9.0 (`ZuNg8qxznV9nd3Bb`). Restoring its exact upstream method alongside the three Fyber receivers reaches the menu and gameplay. Diagnostic `pizza-sdk.apk` contains a temporary `PizzaVM` trace; source-built `pizza-source.apk` has no such instrumentation.
- `BootstrapRestore.kt` and resources under `patches/src/main/resources/pizzabusiness/5.57.3/` now implement all 1459 constants, the native ARM64 reconstruction and four upstream SDK methods. `StartupSupport.kt` depends on this restoration. Native data uses a 1.43 MB compressed copy/literal delta with exact input and output hashes. ARM32 has not been repaired or tested; keep this experimental.
- Updated source build passes (`build-bootstrap-2.log`), clean APKM patch passes (`source-patch.log`), and `pizza-source.apk` is installed on BOTH emulators using the SAME test key and update installation.
- Emitted APK verification passes: all 1459 constants, 1201 unchanged authentication/SDK methods, four exact SDK restorations, five reward-placement scenarios, every asset and library across all 35 input splits (only the reviewed ARM64 library differs). Native reconstruction tests and eight asset verifier regressions pass. CI includes these tests.
- Debug emulator 5556: three force-stop/relaunch cycles, background/screen-lock/resume, airplane-mode off/on and device reboot have produced no new crash. A surviving foreground process is only one check; full gameplay/reward and clean-install retests remain incomplete. Debug root/Frida may need restarting after reboot.
- User explicitly confirmed successful TapBlaze login AND restoration of their previous save. Source-built update preserved Chapter 2 / Day 43, cash 356.24, 21 gems (observed at the main menu). Do not spend/change this progress for testing. Emulator snapshot `pizza-account-before-source` was saved successfully on 5554 after account setup, before installing the source-built APK; recovery only, never restore casually.
- First Google login was actually into stock Play Services (`com.google` account). User subsequently completed the correct MicroG login; AccountManager now has one `app.revanced` and one `com.google` account. No more credentials are currently needed.
- Play Games is NOT verified. Pressing the controller icon under the main-menu hamburger calls `loginGooglePlay`. MicroG receives sign-in type 0 but finds no configured default Games account, returns SIGN_IN_REQUIRED with a PendingIntent. The app's Task reports success and then `requestServerSideAccess` fails without showing account selection. Investigate client resolution handling. Do not treat `autoSelectLogin Accounts is Empty` as proof the MicroG account is missing: it can mean no default Games account for type 0.
- Relevant evidence: `source-games-3.log`, `account-types-after-microg.txt`, `account-menu-loaded.png`, source runtime captures. Raw account logs/screenshots stay local/ignored. Do not print account names or tokens.
- Exact official MicroG sources downloaded as diagnostic references (`GamesConnectService.kt`, `GamesConfigurationService.kt`, `GamesSignInActivity.kt`) and installed 7.1.1 decompilation `MicrogConnectImpl.java` confirm status 4 with a resolution. Game client `zzcv` delegates to `zzaw`; `zzay` is being inspected to find the concrete implementation. MicroG source repository has NOT been modified.

Next: resolve genuine Play Games account selection/authentication, verify user save after force-stop/reboot without changing it, complete clean-install and gameplay/reward tests on debug emulator, document resource provenance, and finish the patch. No commit, push, PR or release has been made.


## Checkpoint: 2026-09-28, 12:59 UTC

- Real MicroG Games authorization succeeded on account emulator 5554 after choosing the existing MicroG account: resolution successful, service authenticated, server-side access and current player retrieved, completeSignIn success. Evidence: games-account-selected runtime capture. Account IDs/tokens stay in ignored evidence. TapBlaze Chapter 2 / Day 43 remains intact.
- Two resolution issues: stock GMS/Play Store version gate is inappropriate for MicroG; SDK is initialized from the login button after Activity.onResume and misses the current Activity. Background/foreground allowed the resolution and proves the lifecycle issue.
- pizza-games.apk bypasses only the legacy version probe; lifecycle fix now moves the existing SDK initialize call into BaseAppActivity.onCreate, before lifecycle events, with authentication and cancellation paths retained. Build/clean patch of pizza-lifecycle.apk underway. Must verify first-attempt resolution, cancellation, repeated real login and save preservation after force-stop/reboot, clean install and gameplay/reward.
- Debug clean pizza-games install reaches Day 1 tutorial without crash; without MicroG, sign-in fails safely and gameplay remains available. Installing official MicroG 7.1.1 on disposable debug AVD to test account selection/cancellation without user account.


## Checkpoint: 2026-09-28, 13:12 UTC

- pizza-lifecycle.apk passes source build, clean APKM patch, 1198 unchanged auth/SDK methods, exact 4 SDK restorations, reward DEX scenarios, 35-split assets (34 libraries/6653 assets), native parity and 8 regression tests. Installed update on 5554 and clean on 5556.
- First-start account resolution now opens without background workaround on clean 5556; canceling the MicroG add-account flow returns to gameplay. 5554 real authentication (server access + current player) passes update and 2 consecutive force-stop relaunches.
- Full 5554 reboot exposed System UI ANR while booting and then a real app crash: IllegalArgumentException Receiver not registered, ck.f0.run, after IntentReceiverLeaked on AppActivity destruction. Evidence lifecycle-after-reboot, lifecycle-reboot-failure and receiver-failure-context.txt. Do NOT mark reboot PASS. A subsequent launch logged in successfully again (lifecycle-reboot-recovered).
- Narrow repair added to StartupSupport.kt: ck.f0 Tapjoy connection worker stores application context for its receiver registration/unregistration instead of Activity context. All SDK callbacks, retries, failures, other UI contexts retained. Build-receiver succeeds; pizza-receiver.apk clean patch in progress. Must fully retest this version, including Activity recreation with connection outstanding, offline/recovery, reboot, save preservation and gameplay/reward.
- Diagnostic 5556 clean tutorial reached sauce/cheese preparation, no completed day yet; pizza needs transfer to oven. User 5554 save must remain untouched except login/restarts.


## Checkpoint: 2026-09-28, 13:22 UTC

- pizza-receiver.apk is current source-built candidate, installed on both AVDs (clean 5556, update 5554). Build, clean patch, integration/reward/all-split verification pass. SHA-256 APK: 568f1f5d7a520cfbeefd8dddf4226473b9d8a73a97cecba343144bad94f882f4. Bundle hash in receiver-artifact-hashes.json.
- Real account login passes two further force-stop starts AND full 5554 reboot; full server authorization/current player succeed after boot services settle. New reboot did not reproduce previous receiver crash; PID 2027 remains alive. Main-menu cash remains 356.24, chapter/day needs final visual/local comparison. Repeating reboot again.
- Clean 5556 opens MicroG sign-in, cancellation returns to Day 1 tutorial, offline/online recovery keeps PID. Setting always_finish_activities alone did NOT destroy the Activity (dumpsys STOPPED), so do not count that as the receiver recreation regression. Setting restored to absent; font_scale restored to1.0, airplane mode disabled.
- Explicit regression helper artifacts/session-20260928/test-receiver-recreation.py waits for Tapjoy connection then changes font scale to force Activity recreation and restores it. First helper failed to observe Tapjoy because MicroG login was foreground; updated helper cancels only observed MicroG foreground activities, rerun pending. This is test setup failure, not app crash.
- Tutorial on prior lifecycle candidate got through oven; input is camera swipe and TAP pizza to transfer, not dragging it. No completed day/reward test yet; current receiver candidate tutorial must be completed.
- Debug MicroG first setup had its own CheckinService ForegroundServiceDidNotStartInTimeException at13:02; capture preserved in debug-before-receiver-crash-buffer.txt. No Pizza crash there. Monitor for recurrence, do not claim dependency crash never occurred.

- 13:25 UTC: second full reboot of receiver candidate 5554 also passed genuine server/player auth. OpenCV comparisons of progress/cash/gems regions to original confirmed all three are pixel-identical after BOTH reboots. Evidence receiver-reboot-2-authenticated and reboot-2-save-comparison.json.
- Debug recreation helper again timed out before Tapjoy because unauthenticated MicroG owned the foreground; no new Pizza crash. Temporarily disabled ONLY debug5556 MicroG with pm disable-user, to isolate SDK recreation/gameplay and service-unavailable recovery. MUST re-enable it at end. Font scale restored1.0, airplane disabled.

- 13:30 UTC: receiver regression now has a true before/after proof. With debug MicroG temporarily disabled to isolate SDK initialization, offline + font_scale1.1 during Tapjoy connection recreates Activity. Fixed APK keeps PID10918, logs onCreate twice, no leaked receiver/crash (fixed-recreation-*). Previous pizza-lifecycle APK reproduces IntentReceiverLeaked and the same fatal Receiver not registered in PID11971 (baseline-recreation-*). Restored pizza-receiver.apk afterward on debug5556; font_scale1.0, airplane off. MicroG remains disabled on debug only until gameplay tests finish.
- User5554 offline launch showed proper Server Connection Failed warning with saved state; restoring network and relaunch completed genuine authorization/player retrieval. Final current PID5258. Background/sleep/wake/resume also preserved process.
- Both latest full reboots preserved exact pixel regions for Chapter2 Day43/cash356.24/gems21. User account path now passes repeated genuine auth, updates, two force-stops, two full reboots, background/resume, offline/recovery. Remaining work: finish real tutorial/day/reward and persisted debug progress, restore debug MicroG, final evidence/report/source review.

- Tutorial control discovery: use ADB input draganddrop 1047 1025 1720 630 1500 (hold then drag paddle handle) to move prepared pizza into oven; simple taps add topping or pan the camera. Current debug final APK reached oven and opened Oven Booster by tapping550,900; next inspect and accept actual eligible video offer.

## Checkpoint: 2026-09-28, 14:25 UTC

- Receiver candidate completed real Day 1 gameplay on debug 5556 (prep/bake/cut/serve/delivery/night results), reached Day 2, cash66.98/gems11. Oven video offer enabled boost without spending7cash; tip offer gave30min; sponsor offer gave exactly1gem (10->11) across Home/resume. Evidence day1-summary, reward-real-after, reward-tip-after, day2-confirmed-header. Day2 save later survived force-stop and full reboot.
- MicroG5556 re-enabled; airplaneoff/font1.0/always_finish absent. User5554 remains untouched with Chapter2 Day43.
- A new edge case was seen: force-stop while notification permission was pending left a PermissionController task. Cocos rejected the next non-root Activity, but our early SDK hook nevertheless initialized Games. Later MicroG cancellation produced one black renderer, recovered via Home/resume. Full evidence receiver-debug-black-after-permission/debug-black-pid15122.log retained. Do NOT count that start as pass or conflate it with a fatal crash.
- Reproduced the concrete root-window violation with a controlled notification request (Frida on DEBUG ONLY), then force-stop/MAIN-LAUNCHER start: baseline-orphan-permission.log shows Cocos rejecting the window followed immediately by Games AutomaticGamesAuthenticator startWatching. Baseline repeated cancellation subsequently rendered normally; black symptom not yet reproducible deterministically. Three interrupted cold starts reached rendered loading; third confirmed main-menu cash afterward. Explicit menu sign-in cancellation also returned correctly.
- Source fix moves SDK initialization after the game's existing accepted-root Activity assignment, still before onResume. No new lifecycle fallback or authentication override. Current NEW candidate pizza-accepted-activity.apk SHA25621c0e913150194d710152fc16db807893fed099fa062a11e2ff0eb99a1bf7b63; MPP SHA2561f76dbc6ac67ddcdfaeb9da20e046d4377030d53d1f446fa7e05e4be279c38ec. Source build and clean patch PASS; 1459constants/1198auth-SDK/4receivers/5rewardplacements/35splits/34libraries/6653assets verification PASS.
- Installing NEW candidate on5556 now; 5554 still runs previous receiver candidate. Next: repeat orphan-permission scenario and confirm rejected window does not initialize Games; valid start and cancellation/save/reward work; update5554 same key and repeat genuine auth/force-stop/reboot/user-save matrix. New candidate needs runtime retest before delivery. No commit/push/PR/release.
- Launch cold starts using MAIN + LAUNCHER, wait for sys.boot_completed after reboot. Booting device can temporarily return activity-not-found before package manager is ready. A notification dialog surviving killed caller requires dismissal before the real game Activity can become root.
- Frida server /data/local/tmp/pizza-frida-server currently running only on rooted5556. Its -D call kept adb client open; client alone terminated, server remains. Request helper artifacts/session-20260928/request-notification.py invokes actual Activity.requestPermissions(POST_NOTIFICATIONS,990), after clearing user-set/user-fixed flags on5556. It attaches by adb PID. Do not run against5554.
