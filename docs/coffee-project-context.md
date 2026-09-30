# Good Coffee, Great Coffee - patch context

Target: `com.tapblaze.coffeebusiness` **1.24.0 (1397)**, complete APKM, ARM64.
Status: experimental. Gameplay and a requested rewarded action were exercised on
Android; Google sign-in, cloud restore and paid checkout are not verified.

## Implementation

Three selectable patches mirror the Pizza workflow while adapting to Coffee's
Unity 6000.3.10f1 / IL2CPP metadata39 architecture:

- **Coffee startup support** restores native initialization, 1447 protected
  string constants, five startup wrappers and four protected SDK receivers.
  SHA-256 guarded PZB2 reconstructions cover libunity and libil2cpp. Native imports
  are linker relocations rather than captured process addresses.
- Sideloaded Unity asset packs are exposed as actual files in Coffee's own
  `no_backup/morphe-coffee-1397` directory. The 2386-entry manifest verifies every
  extracted file before writing its completion marker. Unknown pack requests
  retain original behavior. Game saves and original assets are not rewritten.
- **Skip rewarded ads** delivers the game's displayed/rewarded/closed callbacks
  on Android's main thread after an explicit reward request. It guards readiness
  and duplicate show requests and permits reloading. It does not fabricate paid
  purchases, receipts, SDK impressions or SDK revenue events. The game's original
  analytics callbacks remain intact.
- **Google Play Games via MicroG-RE** redirects the two Games clients to
  `app.revanced.android.gms` and uses the original signing-certificate SHA-1.
  The real PendingIntent resolution, cancellation and authorization remain.
  Real account sign-in/cloud restoration still require verification.
- The original Firebase C++ SDK's embedded app DEX is included unchanged in the
  main class loader. This addresses Android 15's cross-class-loader ARM JNI
  registration problem (AOSP b/393035780), observed as a crash at
  `JniResultCallback_nativeOnResult` in the older Play Store emulator image.
  Native Firebase code is unchanged. Earlier System.loadLibrary preloading alone
  did not fix the issue and was removed.

## Evidence and scope - 2026-09-28

All private evidence is under ignored `artifacts/coffee-20260928/`.

- Gradle `:patches:buildAndroid` succeeds; all three patches apply to the clean
  merged input from the 15-split APKM. `firebase-dex-report.json` records successful
  patching, resource rebuild and signing. Final candidate: `coffee-firebase-dex.apk`.
- Full-SDK asset audit preserves all 2447 original assets and 46 native libraries,
  except the two exact reviewed reconstructions. Thirteen black-box verifier
  tests pass, including multiple libraries and mismatched/missing inputs.
- Emulator5556, Google APIs API35: completed Day1 with two espresso orders and
  the hot Americano tutorial. Reached level2. The end-of-day rewarded button
  increased cash **149.04 -> 159.04** without video and then disappeared.
  Screenshots: `before-reward.png`, `after-reward.png`. In-game ice purchase
  subsequently deducted20 to **139.04** and granted ice (`ice-purchase.png`).
- On-device callback contract test used an isolated listener, not the game's
  save: one loaded/displayed/rewarded/closed sequence, all callbacks on main
  thread, duplicate load/show suppressed, unavailable show ignored, reload ready.
  Evidence: `reward-contract.jsonl`. This complements the real in-game +10 test.
- Emulator5558, fresh disposable Google APIs API35 installation: reached gameplay
  without MicroG, force-stop/relaunch returned to menu. Reboot initially caused
  an Android System UI ANR and a black screen; after dismissing the system dialog
  and relaunching Coffee, menu loaded. Background/foreground also returned to
  menu. Do not describe the initial reboot as an uninterrupted pass.
- Emulator5554, older Google Play API35 with existing MicroG account: prior
  candidates repeatedly crashed in Firebase's native callback. Final DEX variant
  proceeds beyond that crash to loading; final observation recorded below.
  Pizza account and save data were not changed.
- Coffee5556 backups before update include private files/preferences and external
  profileCoffee.dat. Keep those and `artifacts/coffee-test.keystore`; do not reset
  user devices or replace signing material.

Unverified: actual Google/MicroG authentication, cloud-save restoration, long-term
stability on physical ARM64 devices, interrupted extraction recovery, and real
money billing. Coffee's paid offers have not received Pizza's native UI hiding
patch; checkout is not represented as repaired. Purchase verification and tokens
remain original. No real-money charge was attempted.

## Provenance and reproduction

Clean Downloads APKM SHA256:
`61a8e5d22782a4599f1c2fe46ed4e5bee328311e3173db16cff653b1ed305d37`.
Original signing-certificate SHA1:
`aa6e781863cdfb8d32b8f7b368534ace25a0ae7e`.

Native initialization and strings were compared across two original signed-app
runs. Six recovered startup DEX captures match. Fyber8.4.6 / AdQuality9.9.0 SDK
receiver references were checked against Coffee's DEX before reuse from Pizza.
The Firebase app_resources_lib.jar is byte-identical to4342 bytes embedded at
file offset3442128 in original libFirebaseCppApp-12_10_1.so; firebase-app.dex is
its unmodified classes.dex. Resource hashes are in the adjacent provenance file.

Build with Java21, Android SDK, GITHUB_ACTOR and an unprinted GITHUB_TOKEN:
`./gradlew.bat :patches:buildAndroid --no-daemon`.
Patch the clean APKM with Morphe Desktop1.17.0, FULL bytecode mode, the Coffee
signing key and exactly the three Coffee patches above. Never patch an already
patched APK. For the asset audit, pass `--native-delta` twice to
`scripts/verify_pizza_assets.py`, mapping `lib/arm64-v8a/libunity.so` and
`lib/arm64-v8a/libil2cpp.so` to their respective Coffee delta resources.

The SDK lives in `%LOCALAPPDATA%/Android/Sdk`. Test AVDs: Morphe_Pizza_Debug_API35
(5556), Morphe_Coffee_Control_API35 (5558), Morphe_Pizza_API35 (5554). Device
identifiers must be rediscovered next session. Keep raw APKs, logs, saves and keys
out of Git. Unrelated dirty Pizza documentation is intentionally excluded.

## Final commit checkpoint

Final APK SHA256: `5c4b181aef099ec8bf20941664aa8c57afb3819a3d1fb3c84c7b16c9a238e1ef`.
`final-assets-check.json` passes all15 splits,46 libraries,2447 assets for this
exact candidate. Same-key update on5556 preserved Day2 at the main menu
(`final-save-menu.png`). Final Firebase DEX candidate on5554 reached the age
prompt beyond the former native crash (`final-user.png`), with the same process
remaining alive. This is evidence of fixing the observed crash, not a completed
Google sign-in test. Final per-process logs are final-debug-log.txt and
final-play-log.txt. The user requested finishing and committing after actual
runtime success; authentication and billing limitations remain explicit.
Final observation: emulator5554 reached actual Day1 gameplay with Maisie in the
same process8455 (`firebase-main-final.png`), confirming that the observed
Firebase startup crash is resolved in this run. Emulator5556 reached the Day2
notification permission prompt after loading its preserved save.

## Input failures and release retest — 2026-09-30

User reports Manager 1.33.0 on Samsung SM-A546B / Android 16: first ENOSPC during
split merge with 883.60 MB free, then invalid DEFLATE stored-block lengths during
outer archive extraction with 5.40 GB free. Both precede patch execution. Phone
and exact failing/cached input are unavailable over ADB. Local APKM is unchanged
and passes every outer/inner CRC. See `coffee-input-troubleshooting.md`.

Default two-patch selection was reproduced successfully from both the full APKM
and clean premerged APK with the published 1.33.0 MPP. Both reports pass patch,
rebuild and signing; both asset audits pass 15 splits / 46 libraries / 2447 assets.
Startup support is already a dependency of both defaults; no dependency fix is
needed. A read-only archive diagnostic and corrupted-input regression cases are
in `scripts/verify_apkm.py` and `scripts/test_verify_apkm.py`.

Local evidence: ignored `artifacts/coffee-20260930/`. Clean input supplied locally
as `%USERPROFILE%/Downloads/Coffee-1.24.0-clean-merged.apk` (hash in recovery guide).
Final release-default APK was installed with the existing test key on 5556 after
backing up files/preferences to `pre-update-save.tar` (68 tar members). It reached
Day2 gameplay with cash139.04; news reward increased crystals0 ->1 without video.
Screens are in the older screenshot helper's `artifacts/coffee-20260928/` folder:
`final-reloaded.png`, `day2-play.png`, `reward-gem.png`. No save reset took place.

Initial 5% loading on old AVDs was slow, not established as a permanent hang:
both a previous pre-Firebase-DEX build and final release-default build eventually
loaded on5556. The final release-default build was restored after comparison.
The original app on a separate empty control-data.img reached a Play licensing
error, so that control cannot establish gameplay parity. Existing AVD data was
not overwritten. The configured API36.1 AVD has a missing system image; no
Android16 result is claimed. No authentication/cloud success is claimed.
