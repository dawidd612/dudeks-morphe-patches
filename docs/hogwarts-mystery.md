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
- `No energy consumption` checks whether the existing pool can afford zero energy and routes ordinary interactions through the game's free-action branch. It keeps the original action requirement and per-tap progress. The wallet debit, energy-spend reward event and energy-spend analytics in that branch are skipped together. Tasks specifically requiring an energy spend may need the patch disabled. Normal regeneration, paid finish options and the pool serializer keep their original handling. Linked-account synchronization remains unverified. [Jam City energy rules](https://jamcity.helpshift.com/hc/en/41-harry-potter-hogwarts-mystery/faq/718-how-do-i-earn-energy/)
- `HPGoalProgress::start` seeds a local whole-goal wait in seconds after eligibility checks. `Skip local quest waits` zeros that positive countdown inside `HPGoalProgress::passTime` and uses the existing natural-expiry branch and `QuestAvailableNote`. It passes the real elapsed time to other paths. The paid skip uses the same countdown for its price and takes a separate transaction path. TLSQ queue scheduling uses server-anchored time. Objective cooldowns, TLSQ scheduling, active time limits and prerequisite checks keep their original paths. Save synchronization of an accelerated quest remains unverified.
- `FocusVC::onTap` stores the earned grade before scheduling the result screen. `Faster lesson feedback` shortens this 2-second pause to 1 second. It also shortens the ready and learned banner delays from 1.8 and 1.5 seconds to 0.5 seconds, keeping the original close and button-initialization callbacks. The built-in gesture skip directly invokes a success or failure callback. Entire lessons, player timing windows, grade calculation and reward paths remain part of normal play.
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
| Energy Freedom | TESTED AND WORKING | No energy consumption makes ordinary interaction taps free. | In Escape the Devil’s Snare, x86-64 executed nine actions with a combined nominal cost of 40. Each left the pool at 24/24. Five stars and the completion reward were recorded. The built APK also passed 14 native affordability and progress cases and 36 HUD cases across ARM64 and x86-64. | Ordinary interaction taps only. Energy-spend objectives may require the patch to be disabled. |
| Skip Waiting Timers | BUILT / NOT RUNTIME TESTED | Skip local quest waits uses natural expiry for a positive countdown on an eligible quest. | Individual and combined APKs. 192 CPU-emulation cases on original and patched instruction ranges confirmed signed guards, zero on expiry and unchanged elapsed argument. | The test guest did not reach a positive quest wait. Objective cooldowns, timed-event scheduling and synchronization are outside this result. |
| Skip Classes | TESTED AND WORKING | Faster lesson feedback trims the result pause and two post-fade lesson pauses. | x86-64 Focus awarded a perfect grade with a 1-second pause versus 2 seconds in the control. The learned banner used 0.5 seconds versus 1.5 seconds. The lesson still completed and applied its reward. | The ready banner still needs a runtime test. Full lesson skipping is unimplemented. |
| Instant Actions | TESTED AND WORKING | Faster action feedback changes 300 ms to 125 ms. | x86-64 Potions actions used 125 ms of feedback and consumed their actual energy costs. The control used 300 ms in Charms. | Scope is local button feedback. |
| Faster Animations | TESTED AND WORKING | Faster interaction fades changes local pause and fade durations. | Live x86-64 Fast mode ran 0.25-second pause and fade stages for the four book interactions and in Potions. All options passed binary tests. | Normal and Very Fast still need gameplay timing. Scope is action-button feedback. |
| Faster Dialogues | TESTED AND WORKING | Faster dialogue text configures 100 characters per second. | The x86-64 and translated ARM64 guests passed the Rowan-to-player dialogue route. On x86-64, four memory-only frames captured Rowan before the text and the full 98-character line about one second later. Manual advance reached the next turn. The x86-64 guest also selected a normal response from three Rowan choices and received the matching reply. | Timing is approximate. Later scenes and other languages remain untested. |
| Quick Rewards | TESTED AND WORKING | Quicker reward reveals shortens three local presentation delays. | The x86-64 Postbox gift used a 125 ms reveal and credited 10 energy after collection. Native execution from the APK verified all three changed inputs and normal continuation on both ABIs. | One reveal path exercised in gameplay. |
| Clean UI | BUILT / NOT RUNTIME TESTED | Hide automatic paid greeting suppresses greeting type `0x21`. | Native execution verified suppression and preserved the adjacent type `0x22` on both ABIs. | The live offer manager received no eligible `0x21` call. The separate manual `SPECIAL` entry is outside the popup patch. |
| Cosmetics QoL | TESTED AND WORKING | Quicker outfit previews halves the camera transition duration. | A clean x86-64 guest reached the character creator. The live Cocos transition was 0.10 seconds. The preview remained open while switching selections. | Verified in character creation. The later wardrobe remains untested. |

The full Gradle build and `generatePatchesList` passed with Morphe Gradle Plugin 1.3.3. The generated preview indexed Hogwarts Mystery as a ninth app. The seventeen existing patch definitions matched the released metadata exactly. The build kept the existing source, CI and release workflow. The bundle SHA-256 before the zero-cost energy update was `e32839ec4d93eb2a312b646d32e3ebb1a7fb0d02a6564470f78dc58425dad31d`.

Morphe CLI verified the seven original selections and each new selection individually. Desktop 2.0.0 applied all nine together. The independent binary checks verified the 25 existing edit ranges and eight new ranges across ARM64 and x86-64. The nine-patch APK differed from the verified seven-patch APK only at the eight new native ranges. The APK kept all 24 native libraries from the three input ABIs. ARMv7 and the other libraries matched the original payloads. Tests also rejected base-only input and an already modified library without writing a patched APK.

The isolated API 35 x86-64 AVD installed and launched the original split set, then the re-signed combined APK. Same-key upgrade and a cold emulator restart returned to the guest's Diagon Alley progress. Later testing reached Flourish and Blotts and opened the first book interaction. The ARM64-only APK passed signing and 16 KiB checks and launched with `primaryCpuAbi=arm64-v8a` through the x86-64 emulator's native bridge. It loaded the guest's Diagon Alley progress and passed the same dialogue replay. No physical ARM64 device was tested. The nine-patch APK installed as a same-key x86-64 update. The guest completed the four book interactions, answered Rowan, and reached Get Your Wand. The earlier OCR stop occurred at a swipe-only tutorial prompt. A guarded swipe and local highlight detection completed the route. A local read-only probe measured the energy interval and actual Cocos action durations. Linked-account sign-in and cloud recovery remain open.

The calibrated Diagon Alley route passed in four local OCR frames. Resuming at step 2 passed in two frames. The seven synthetic runner tests cover safe replay, resume, unknown screens, OCR ambiguity, sensitive screens and bounds. An emulator snapshot restored offline on this host until cold boot. Resuming with `--from-step` uses the currently running scene and avoids that dependency.

The first-lesson follow-up resumed the same x86-64 guest at Get Your Wand. OCR handled dialogue and the chapter reward screens. A gesture traced along the highlighted wand path earned Ollivander's “Splendid!” response. The guest completed chapter 1 and used `COLLECT`, which opened the chapter 2 start screen. The live energy pool remained at 24/24 with a 30-second interval.

Starting Welcome to Hogwarts stopped at the animated crest twice. The second attempt used the same guest after a process restart with no active probe. The restart restored the final Rowan dialogue in Diagon Alley, so only that exchange and the chapter transition were repeated. The game process remained alive on the crest for over two minutes. Logs showed compressed texture upload errors around the transition and HTTP 403 responses for some static resources during startup. Their relationship to the stall is unconfirmed. With `swangle` and `swiftshader`, the game returned to the Android launcher during startup. Android recorded both as process exits with status 0. After restoring `-gpu host`, a fresh launch after emulator startup returned to Diagon Alley with the same installation and data.

At that checkpoint, the first lesson had yet to exercise energy spending, below-cap refill, action timing, minigame results and reward collection. The chapter 1 reward reached a separate screen from the targeted reward reveal hook.

### Same-key control for the chapter 2 transition

A zero-selection Morphe Desktop 2.0.0 run rebuilt all eleven original APKs as one APK with the same signer as the nine-patch build. SHA-256 comparison of ZIP payloads found differences only in the ARM64 and x86-64 copies of `libpotter.so`. All three control library hashes matched their original splits. The control passed signature and 16 KiB alignment checks. Its APK SHA-256 was `7a5d240138b575d89ed8b3b0f0af9a7b7fcb7b94001d468fdf8e12534111527e`. A private backup preceded `adb install -r` on the same AVD. The app ID and first installation time were preserved.

The zero-selection APK entered the Great Hall twice. The original nine-patch APK also passed on retest from a byte-identical local save at the final Rowan checkpoint. Its transition reached scene completion and left the crest after about 28 seconds. A probe saw no positive quest wait during that run. Nine Cocos cache files appeared during the first successful control transition and remained available for the subsequent runs. The earlier hang has not been reproduced with that cache. The cache change, previous graphics errors and HTTP 403 responses do not establish a cause, so no patch was changed on that evidence.

The zero-selection control completed the first Charms lesson, Lumos, with two stars. Guarded OCR and highlight detection opened the actions. The original button scheduled 300 ms of feedback. The first debit spent 3 energy (24 to 21). The remaining 30-second countdown carried over from the previously installed build and yielded one energy. The following countdown reset to 240 game seconds and also yielded one energy below the cap. The first star reward increased coins from 103 to 203. The Focus minigame received a real touch inside the green timing window, returned a perfect passing grade and used the original 2-second result pause. OCR selected `COLLECT` on the success screen. The game marked the lesson complete, applied the class reward and credited 30 attribute XP.

The nine-patch APK was reinstalled over the control after Lumos. The saved file was byte-identical through the update. Hogwarts Record showed Lumos as learned after relaunch. The separate post-lesson Lumos trace rejected the generated touch paths and was advanced with the game's visible `Skip` button. On the same save, the patched APK completed Cure for Boils with two stars. Watch Merula used 125 ms of feedback, debited 3 energy (24 to 21) and the pool recovered one point about 30 seconds later. All 12 measured debits matched their action costs. Completion pause and fade each took 0.25 seconds. Focus again produced a perfect passing grade after a real tap. The result pause measured 1 second and the learned banner pause 0.5 seconds. The final cauldron trace registered a failed gesture. The game then showed the two-star success screen, applied the class reward of 10 attribute XP on `COLLECT`, recorded project completion and displayed the level-up screen. A cold app launch with the nine-patch APK and the probe detached returned to the selected post-lesson reply. OCR replayed the remaining dialogue and verified the next objective, Report to Angelica.

### Zero-cost energy and Chapter 3 follow-up

`No energy consumption` replaces `Faster energy regeneration` in the development bundle. The full Gradle build and `generatePatchesList` succeeded. The generated preview had nine Hogwarts definitions and matched the seventeen existing definitions exactly. The new `patches/build/libs/patches-1.39.1.mpp` SHA-256 is `1ee2c59bf243ca80dcf98b71c200db3f209ceeedf137dc61c3b5f936073f076e`. Morphe Desktop applied the nine patches to the original APKM. The APK passed the same-certificate and 16 KiB alignment checks. Its only payload changes from the previous build were the new zero-cost instructions and the restoration of the original energy regeneration instructions. A local backup preceded the in-place update and the saved file retained its exact bytes during installation.

On the x86-64 AVD, OCR resumed Report to Angelica, completed the House Points tutorial and advanced to Collect Slugs. In Escape the Devil’s Snare, all nine energy-priced actions advanced with 24/24 before and after each action. The total nominal cost was 40, with zero calls to debit or refill. The game awarded five stars, completed the project and applied the reward through `COLLECT`. The run used the visible built-in `Skip` for repeated Lumos gestures. The previous build failed the same balance assertion on all twelve recorded paid actions.

Instruction tests on the APK reran 192 timer cases across both ABIs. The targeted whole-quest timer did not receive a positive countdown on the tested route. The eight-hour activity time limit continued counting down. The Devil’s Snare reward used the project completion view. A cold launch after the escape loaded the saved progress and unlocked the Postbox. The welcome gift reached the targeted `CollectRewardsVC` path and measured a 125 ms delay. The final `COLLECT` closed the reveal and raised the real balance from 24 to 34. Native execution verified all three delay inputs and normal continuation. The live offer manager ran without an eligible `0x21` call, and the separate manual `SPECIAL` tile remained visible. The game was left at the second Report to Angelica objective in Year 1, Chapter 3, part 3 of 6.

```powershell
python -m unittest discover -s scripts -p test_hogwarts_smoke.py -v
```
