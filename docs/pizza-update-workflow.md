# Good Pizza, Great Pizza update workflow

Keep compatibility changes small, but only publish a target after its initialization resources and runtime behavior are verified. Adding a version number does not port the existing native bootstrap.

## Intake

1. Obtain the clean, publisher-signed APK/APKM/XAPK. Keep APKs, logs, account data and signing keys out of Git.
2. Record the archive SHA-256, package name, manifest versionName/versionCode and all split names. Validate every ZIP entry and retain the asset, ARM64 and density splits.
3. Record the SHA-256 of ARM64 libcocos2dcpp.so. Compare it with the input hash in the matching PZB2 native delta, not merely the previous archive hash.

## Minimal update decision

- If the native input hash and every bytecode/bootstrap guard still match, reuse those proven resources, add the explicit target and run the existing verifiers.
- If the native hash differs, retain the old target and guards. Capture initialization from two independent runs of the new original signed build on a disposable ARM64-capable test device/emulator. Reconstruct the new library with scripts/reconstruct_pizza_native.py and generate its delta with scripts/build_pizza_native_delta.py.
- Capture and compare the new build's static string initialization. Recover SDK receiver bodies only from the exact matching official SDK artifacts.
- Store new initialization resources in a version-specific directory; select them by an explicit target mapping. Update only changed fingerprints and layouts. Never apply the old delta to a different library or substitute the entire old game library.
- Inspect HidePaidOffersPatch.kt separately: its native hash/layout checks also require validation against the newly reconstructed library.

## Verification and publication

1. Build the source bundle and patch the complete clean input.
2. Run the bootstrap, reward DEX, integration DEX and all-split asset/library verifiers. Review only the intended native changes.
3. Install with the existing test key without clearing user data. Use a disposable environment for clean-install testing.
4. Verify repeated launch, force-stop, reboot, background/resume, offline/recovery, gameplay, rewards, genuine Google Play Games/TapBlaze authorization and saved-progress persistence.
5. Record evidence and exact remaining blockers in docs/pizza-project-context.md. Commit and publish the release only after the new target passes; static checks alone do not establish runtime success.

