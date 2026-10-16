# Coffee update workflow

The last fully identified input is Coffee **1.25.1 / 1454**, APKM SHA256
`7172bcfad15a35bef16f93f2da91f13675c1f22a790d969a3fbceaf1e6c081d3`.
Preserve each version's profile; never reuse native deltas merely because Unity
has the same version. APKs, captures, saves, system libraries and keys stay under
ignored `artifacts/` and `tools/`.

## What is faster

One version profile owns compatibility, startup token, R8 Games client mappings
and restoration resources. The three selectable patches share that profile.
Initialization capture discovers native VM tokens from the actual mapped ELF
base instead of hand-copying token strings. Capture finishes on receipt of both
libraries rather than waiting a fixed sleep. Resource generation is reproducible.

The audited staging cache is keyed by the complete APKM SHA256 and verifies every
cached output before reuse. On this 439959161-byte input, a cold audit/stage took
**3.006 s**, and a verified repeat took **0.891 s**, a **3.37x** speedup for this
stage. These timings include source hashing. This is not a measured 3x speedup
of an entire future port: SDK changes and actual game/authentication tests still
need independent work. Never skip those tests to meet a speed target.

## 1. Prepare and inspect once

```powershell
python scripts/prepare_coffee_update.py <clean.apkm> --output artifacts/coffee-update
javac -cp tools/morphe-desktop-1.17.0-all.jar -d <evidence> scripts/InspectCoffeeUpdate.java
java -cp "tools/morphe-desktop-1.17.0-all.jar;<evidence>" InspectCoffeeUpdate <cache>/splits/base.apk > <evidence>/inventory.txt
```

The cache path is `artifacts/coffee-update/<full-sha256>/`. It contains the
CRC-checked split APKs, ARM64 libraries and base DEX, plus asset/library hashes.
`prepared.json` is written only after the complete audit. Corrupt cache entries
are regenerated from the unchanged source; incomplete archives get no marker.

Compare SDK versions and signatures, native hashes, asset split layout and
recovered wrappers. For 1.25.1, Ad Quality changed to **9.10.0**, Singular stayed
**12.6.1**, and the startup capture contains **six** wrappers (including
CoffeeApplication). The old Ad Quality callback has incompatible references.
The 1.25.1 profile explicitly maps `mg6`, `wg5`, parent `wi1`, resolution `fl5`
and retry holder `hk5`. These are evidence for this build, not future defaults.

## 2. Enforce silence before every game launch

Use an existing disposable, debuggable Google APIs ARM-translation AVD, or create
a new one. Never reset a user's AVD. The launcher verifies the named AVD/serial,
`-no-audio`, disabled audio devices and media volume zero. It opens no host window.

```powershell
./scripts/Start-CoffeeMutedEmulator.ps1 -Avd Morphe_Coffee_Update_API35 -Port 5562
./scripts/Start-CoffeeMutedEmulator.ps1 -Avd Morphe_Coffee_Update_API35 -Port 5562 -VerifyOnly
```

Do not launch the game if the verification command fails. Recheck after reboot.
The script may restart adbd as root on the named debuggable test AVD to control
its media volume. It does not clear or uninstall apps.

## 3. Capture original initialization twice

Install the original publisher-signed **complete split set** on the disposable
capture AVD. Start the matching local Frida server. Python needs `frida`,
`pyelftools` and `lief`; this workstation has them under `tools/python`.
The agent build uses the existing `tools/frida-agent` esbuild/Java bridge.

```powershell
$env:PYTHONPATH='tools/python'
python scripts/capture_coffee_update.py --evidence <evidence> --inputs <cache>/inputs --inventory <evidence>/inventory.txt --label native-2 --serial emulator-5562 --avd Morphe_Coffee_Update_API35
python scripts/capture_coffee_update.py --evidence <evidence> --inputs <cache>/inputs --inventory <evidence>/inventory.txt --label native-3 --serial emulator-5562 --avd Morphe_Coffee_Update_API35
python scripts/reconstruct_coffee_capture.py <evidence> --serial emulator-5562
```

Require independent ASLR bases, equal constant snapshots, equal executable
changes, and zero unresolved native imports. The reconstruction checks imports
against both observations. Its IFUNC mapping is reviewed for the pinned API35
libc SHA256; another libc needs explicit resolver analysis. Do not replace an
unknown pointer with a guessed address. Replaying this process reproduced the
tested 1.25.1 native outputs byte-for-byte.

## 4. Generate and validate the profile

Recover wrappers with `RecoverCoffeeResources.java` from current captured DEX.
Its arguments are: evidence directory, output startup DEX, previous Coffee SDK
DEX, matching reviewed SDK DEX, output SDK DEX, current base APK. It replaces the
Ad Quality subset while retaining other SDK restorations. Do not pass an older
vendor version merely because its classes have similar names.

Run `ExtractPizzaSdkReceivers` against the current base APK and candidate SDK
DEX to validate actual referenced methods/fields and recover only current
protected receivers. For 1.25.1 all five validated. The matching 9.10.0 vendor
artifact is documented in `pizza-5580-provenance.json`; Singular provenance is
in `coffee-sdk-restoration.md`.

```powershell
python scripts/build_coffee_profile.py <evidence> --splits <cache>/splits --output patches/src/main/resources/coffeebusiness/<version>
```

The generator consumes `sdk-receivers.dex` and `startup-methods.dex` in evidence,
both string snapshots and reconstructed native libraries. It extracts the
original embedded Firebase DEX, builds the current pack manifest, emits two PZB2
deltas and resource hashes. All seven 1.25.1 outputs replayed byte-for-byte.
Add a reviewed `profile.properties` and its directory to `profiles.txt` only when
the exact APK version/code, startup token and Games mappings are identified.
Start new targets as experimental. Promote a reviewed target when the release
owner authorizes official support; document remaining verification limits even
after promotion. Coffee 1.25.1 was promoted at the user's request on 2026-10-16.

## 5. Build, patch and verify

Set `ANDROID_HOME` to the installed SDK; use Java21 and existing GitHub package
credentials without printing tokens. Build `:patches:buildAndroid`, then patch
the clean original APKM with the two default Coffee selections in FULL mode.
Use the existing signing key for updates. Capture a JSON patch report.

Run `VerifyCoffeeProfiles.java` against the built MPP, the preparation/cache
tests, the archive-checker tests and asset-verifier tests. Run the actual asset
audit against both current native deltas, and `apksigner verify` on the output.
Patch the previous supported version as a compatibility regression. Avoid full
SDK rescans or decompiles when hashes and targeted checks already answer a question.

## 6. Runtime and publication evidence

After the mute check: clean install in a disposable profile, real gameplay,
isolated reward callback contract plus a requested in-game reward, repeated
force-stop starts, Home/resume, offline/online and reboot. Back up private and
external files before a same-key upgrade of an existing test save. Verify its
day, balances and inventory through the UI. Keep raw logs and exact input/output
hashes with every failure. Slow or black intermediate screens need observation
and diagnosis; they are not a pass.

Real TapBlaze/Google credentials and verification codes remain user actions.
Opening a login form does not prove account restoration. Publish only the patch
bundle and source tooling, with measured outcomes and remaining limitations;
never upload game APKs, raw captures, signing keys or saves.
