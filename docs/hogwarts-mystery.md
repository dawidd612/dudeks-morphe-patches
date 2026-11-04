# Harry Potter: Hogwarts Mystery

The experimental QoL patches target Hogwarts Mystery 7.3.4 (7030400), build `a602b75`, on ARM64 and x86-64. Each patch is opt-in. The original APKM also contains an ARMv7 split.

## Verified input

- File `com.tinyco.potter_7.3.4-7030400_3arch_7dpi_ab10f870af9fe723064344403bbf2545_apkmirror.com.apkm` (185,758,000 bytes).
- APKM SHA-256 `95f7c3b7540e4b733256c0a38f17def020d0a90fd375b9f1463b637b5daa42dd`.
- Package `com.tinyco.potter`, versionName `7.3.4`, versionCode `7030400`, minSdk 28, targetSdk 36.
- All eleven APK signatures passed Android SDK 36.1.0 `apksigner verify`. The signer certificate SHA-256 is `4b214a464c7365f1317deab3b0fccecd03c051f67fa9161293fac7ed394a62af`.
- Contents comprise `base.apk`, `split_config.{arm64_v8a,armeabi_v7a,x86_64}.apk` and `split_config.{ldpi,mdpi,tvdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}.apk`. The seven DPI splits cover 120–640 dpi. There are no separate locale or asset-pack APKs in this archive. APKMirror metadata, an icon and archive signature files are also present.
- The repository's `scripts/verify_apkm.py` validated the outer archive and each inner APK. Patching used a temporary copy of the input from Windows Downloads.

| Native library | SHA-256 |
|---|---|
| ARM64 `libpotter.so` | `dbf76481e639efebd57beb2ae8c84bd74dab507e4cc029c2f1bd5dfde5c6e22e` |
| x86-64 `libpotter.so` | `dbacc390a56799093562bd356e2d7141038eb826d06813e790d1d829d55658e1` |
| ARMv7 `libpotter.so` (inventory only) | `6ff1c35ea9e6d6198736d48b59a6d84dca863ddab2c2703e3024984212a3cf35` |

Each ABI APK includes `libpotter.so` plus AppLovin, Crashlytics, AndroidX DataStore and Tapjoy native libraries. The base includes four DEX files and the built-in Cocos assets.

## Analysis and scope

Cocos2d-x and the `Bpc`/Griffin gameplay code are compiled together in `libpotter.so`. Exported C++ symbols and ARM64/x86-64 disassembly expose the game code. The Android entry point is `com.tinyco.potter.HPGameActivity`. Cocos view JSON describes buttons, text and presentation. Unity Ads in Android logs is an SDK. The archive contains neither a Unity player nor an IL2CPP gameplay library. The embedded build info reports protobuf schema version `7.3.0.0`.

- `InteractionActionButton::canAfford` checks energy before a tap. `ScenarioHandler::onActionUpdate` calls `EnergyPool::tryRemoveEnergy` and separately counts the actual cost for quest multipliers, analytics and events before advancing the action. `EnergyPool` loads and serializes the balance and countdown through `GriffinMessages`. Skipping only the debit would split the balance from spent-energy accounting. The game's temporary unlimited-energy mode emits a separate spend event and has its own persisted state tied to legitimate bonuses. [Jam City lesson mechanics](https://jamcity.helpshift.com/hc/en/41-harry-potter-hogwarts-mystery/faq/728-how-can-i-make-my-character-participate-in-class/) · [Energy Toffees](https://jamcity.helpshift.com/hc/en/41-harry-potter-hogwarts-mystery/faq/4510-what-are-energy-toffees/)
- `EnergyPool::refreshSettings` obtains the configured regeneration interval after booster calculation. The patch scales that interval before storage. `passTime` retains the original countdown, `addEnergy`, cap and earned-event path. The default four-minute interval becomes 30 seconds. x86-64 rounds fractional seconds up. Server reconciliation of the faster balance across accounts and long sessions remains unverified. [Jam City energy rules](https://jamcity.helpshift.com/hc/en/41-harry-potter-hogwarts-mystery/faq/718-how-do-i-earn-energy/)
- Class start and completion views expose duration choices, star requirements, extension purchases and rewards. Action code has separate incremental and paid finishing paths. No safe skip was established for lessons or minigames. Task state and cooldowns intersect with protobuf state and server-time anchors. No specific mission timer was proven both local and safe to shorten.
- `InteractionActionButton::updateStats` schedules the progress bar and completion callbacks with the same duration. `onAdvanceProgress` builds a local delay, fade and callback in sequence. Each patch changes the duration inputs for its own function.
- `HPTypewriterLabel::applyDefaults` controls the rendered text rate for both the configured and fallback paths. Dialogue choices and manual advancement use the original handlers.
- `CollectRewardsVC::continueReveal` schedules short presentation delays. The patch reduces three delays from 0.5 to 0.125 seconds and keeps the scheduled actions in order. `GreetingManager::queuePurchasableGreetingModal` only queues one paid modal. Three call sites per ABI ignore its return. Suppression does not record a view or dismissal, so an offer can remain eligible elsewhere.
- `CharacterViewerNode::updateConstraints` passes a camera duration to `AvatarBuilderSlotTransition`. The patch halves it from 0.2 to 0.1 seconds. The game still determines the target pose and inventory permissions.

All patches share one package/version and clean library hash check. The writer checks expected bytes for both present 64-bit builds before changing either. A missing supported library or mismatched hash stops patching. Morphe raw resources carry the edits without a DEX extension or game assets in the bundle.

## Community references

HMODs advertised Skip Classes and Infinite Energy for 6.5.2 on Sbenny. The download is gated and the public description supplies neither licensed implementation code nor evidence for 7.3.4. A historical 4PDA index mentions Instant Actions for 2.3.1/2.4.0. No inspectable Faster Gameplay implementation for 7.3.4 was found. JoDylan404's public project automates BlueStacks taps externally. This implementation uses disassembly of the supplied APKM. [HMODs thread](https://forum.sbenny.com/thread/harry-potter-hogwarts-mystery-v6-5-2-ultra-mod-menu-free-shop-skip-classes-unlocked-items-infinite-energy-and-more-updated.187419/) · [4PDA](https://4pda.to/forum/index.php?showtopic=885116&st=1720) · [JoDylan404](https://github.com/JoDylan404/hogwarts-mystery-script-auto-clicker)

## Build, patch and install

1. Run `./gradlew :patches:buildAndroid --no-daemon` (Windows `.\gradlew.bat :patches:buildAndroid --no-daemon`). The bundle is under `patches/build/libs/`. Use the local bundle for this branch. Semantic-release regenerates the published index when changes ship.
2. In Morphe Manager, use Sources → + → Local to add the development bundle. Enable Experimental app versions for that source and turn on Expert mode. After release, use the existing Dudek's Morphe Patches source. Select the full original APKM and enable the desired patches. Keep the ARM64 or x86-64 split for the target device. [Local sources](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patch-sources.md) · [Morphe Manager Expert mode](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patching-expert-mode.md)
3. Export the merged APK and install it on a disposable device first. The Morphe Desktop 2.0.0 output passed signature verification and `zipalign -c -P 16 4`. Desktop 1.17.0 produced a working 4 KiB output on the test emulator, but its two patched libraries failed the 16 KiB ZIP alignment check. [Morphe Desktop releases](https://github.com/MorpheApp/morphe-desktop/releases) · [Android ZIP alignment](https://developer.android.com/tools/zipalign)

```powershell
java -jar MORPHE_DESKTOP.jar patch ORIGINAL.apkm "--patches=PATCHES.mpp" --exclusive "--enable=Faster dialogue text" "--keystore=HOGWARTS.keystore" "--out=HOGWARTS.apk"
adb -s TEST_SERIAL install -r HOGWARTS.apk
```

To disable a patch, apply the updated selection to the original APKM and install with the same signing key. Keep that key outside Git. Android rejected updating the official APK with the Morphe signature (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`). Use an isolated installation or verify save recovery before migrating. [Android app signing](https://developer.android.com/studio/publish/app-signing)

Jam City documents Google linking across Android and PC and a separate Facebook link for recovery. Verify a recoverable link before changing an existing save. A Player ID can assist Jam City support. Google Play Games OAuth clients depend on the registered package and certificate. A linked account was unavailable for testing. [Google linking](https://jamcity.helpshift.com/hc/en/41-harry-potter-hogwarts-mystery/faq/9406-google-account-linking-faq/) · [Facebook recovery](https://jamcity.helpshift.com/hc/en/41-harry-potter-hogwarts-mystery/faq/875-how-do-i-load-a-game-that-was-linked-to-facebook/) · [Player ID](https://jamcity.helpshift.com/hc/en/41-harry-potter-hogwarts-mystery/faq/873-where-can-i-find-my-player-id/) · [Google Play Games client setup](https://developer.android.com/games/pgs/console/setup#generate_an_oauth_20_client_id)

## Repeatable Windows test

Use a dedicated AVD with `-no-window -no-audio`. The host runner checks the AVD name, package version and focused window. Windows OCR processes frames in memory. The runner prints only allowed labels, coordinates and step results. Keep the disposable guest between runs. Use `--from-step` to start at the current stage of a calibrated route. The runner still verifies that stage before tapping. For a different patch selection, use the same key and `adb install -r`. Keep emulator data outside Git.

```powershell
python scripts/hogwarts_smoke.py --serial emulator-5580 status --allow "Rowan"
python scripts/hogwarts_smoke.py --serial emulator-5580 replay scripts/hogwarts_diagon_smoke.json
python scripts/hogwarts_smoke.py --serial emulator-5580 replay scripts/hogwarts_diagon_smoke.json --from-step 2
```

The default AVD is `Morphe_Hogwarts_QoL_API35`. Set `--avd` for another isolated emulator. `--red-channel` is optional and requires Pillow. It compensates for red-only output from the test SwiftShader renderer. A route is a JSON array of steps. Each step checks unique, exact OCR labels. It can then tap a uniquely recognized label or use a coordinate tied to the capture dimensions. Account and purchase screens stop replay.

```json
[
  {"expect": ["Rowan"], "point": [1268, 949], "size": [2400, 1080]},
  {"expect": ["You"]}
]
```

## Verification report

| Patch or requested behavior | Status | Implementation | Tests | Limits |
|---|---|---|---|---|
| Energy Freedom | EXPERIMENTAL | Faster energy regeneration scales the genuine cooldown by 1/8. | Individual and combined patching. Both ABI outputs checked against original splits. | Unlimited energy remains unavailable. Runtime refill, cap, and synchronized balance still need a controlled lesson test. |
| Skip Waiting Timers | NOT IMPLEMENTED | Local UI pauses are covered by the animation and reward patches. | Static analysis of cooldown and save paths. | No safe mission-gate edit was identified. |
| Skip Classes | NOT IMPLEMENTED | Original lesson and minigame paths. | Static review of actions, progress and completion. | No safe way to bypass repetition while retaining choices, stars and reward semantics was proven. |
| Instant Actions | BUILT / NOT RUNTIME TESTED | Faster action feedback changes 300 ms to 125 ms. | Individual and combined patching. The combined APK opened the first book action in the tutorial. | Action duration and completion callbacks still need a targeted timing and reward test. |
| Faster Animations | BUILT / NOT RUNTIME TESTED | Faster interaction fades changes local pause and fade durations. | Normal, Fast and Very Fast each passed individual patching and binary comparison. Normal preserved all original library bytes. | Scope is action-button feedback. |
| Faster Dialogues | WORKING | Faster dialogue text configures 100 characters per second. | The x86-64 and translated ARM64 guests passed the Rowan-to-player dialogue route. On x86-64, four memory-only frames captured Rowan before the text and the full 98-character line about one second later. Manual advance reached the next turn. | Timing is approximate. Later choices, languages and scenes remain untested. |
| Quick Rewards and Clean UI | BUILT / NOT RUNTIME TESTED | Quicker reward reveals and Hide automatic paid greeting are separate patches. | Individual and combined patching. | The tested guest did not reach the targeted reward and paid-modal events. |
| Cosmetics QoL | BUILT / NOT RUNTIME TESTED | Quicker outfit previews halves the camera transition duration. | Individual and combined patching. The combined APK passed character creation. | Timing while browsing wardrobe items remains unverified. |

The full Gradle build and `generatePatchesList` passed with Morphe Gradle Plugin 1.3.3. The generated preview indexed Hogwarts Mystery as a ninth app. The seventeen existing patch definitions matched the released metadata exactly. The build kept the existing source, CI and release workflow. The bundle SHA-256 was `6dc0eb2f3fb16e6ca40409a4795d45503717545d7a242160e9c1a666b6695196`.

Morphe CLI applied each of the seven patches alone and all seven together. The combined libraries differed only in the 25 expected ranges across ARM64 and x86-64. The APK kept all 24 native libraries from the three input ABIs. ARMv7 and the other libraries matched the original payloads. Tests also rejected base-only input and an already modified library without writing a patched APK.

The isolated API 35 x86-64 AVD installed and launched the original split set, then the re-signed combined APK. Same-key upgrade and a cold emulator restart returned to the guest's Diagon Alley progress. Later testing reached Flourish and Blotts and opened the first book interaction. The ARM64-only APK passed signing and 16 KiB checks and launched with `primaryCpuAbi=arm64-v8a` through the x86-64 emulator's native bridge. It loaded the guest's Diagon Alley progress and passed the same dialogue replay. No physical ARM64 device was tested. The guarded book tutorial stopped when OCR could no longer identify the next action uniquely. Full lesson completion, refill timing, post-lesson rewards, linked-account sign-in and cloud recovery remain open.

The calibrated Diagon Alley route passed in four local OCR frames. Resuming at step 2 passed in two frames. The seven synthetic runner tests cover safe replay, resume, unknown screens, OCR ambiguity, sensitive screens and bounds. An emulator snapshot restored offline on this host until cold boot. Resuming with `--from-step` uses the currently running scene and avoids that dependency.

```powershell
python -m unittest discover -s scripts -p test_hogwarts_smoke.py -v
```
