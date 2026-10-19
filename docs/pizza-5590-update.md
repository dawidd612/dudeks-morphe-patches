# Pizza 5.59.0 update checkpoint

This is an incomplete port, not a supported target or a test release.

Input: publisher-signed APKPure XAPK 5.59.0 (2322), 455226166 bytes.
Archive SHA256: 085a7462ec672c22bebbefc0d79a0480ae1d68b72cb3ad7f6b5ecc501b39b99d.
All five embedded APK signatures verify against the existing TapBlaze certificate.
Every archive entry and embedded APK entry passes CRC validation.

The new ARM64 native library and startup program do not match either existing
profile. There are 1477 uninitialized strings in 32 holders, and 22 protected
methods. None of the old string-holder names matches this build. Changing only
compatibility metadata or applying the old initialization delta cannot port it.

Six SDK methods have been recovered from checksum-verified official SDK artifacts,
with target field/method reference validation. They are stored in an inactive
5.59.0 resource directory. The directory is deliberately absent from profiles.txt.
Store visibility instruction locations have been matched in the original library;
its reconstruction can shift offsets, so no new store delta has been generated.
See pizza-5590-provenance.json for exact inputs, SDK sources and pending work.

Checks completed: archive audit, publisher signatures, 13 asset-verifier regression
tests, 6 archive-audit tests, Java verifier compilation, SDK reference validation.
The existing v1.35.4 bundle skips all three patches for 5.59.0 (appliedPatches=[]).
A successful CLI exit from that diagnostic merge is not patch-support evidence.

A disposable software-emulated API30 Android image with ARM64 translation is
under diagnosis. No new native/string initialization capture, patched-game
launch, authentication or save-persistence result is available.

Remaining: two independent original-signed initialization captures; exact native
reconstruction and import resolution; protected SDK review; reconstructed store
delta; full source build and clean-XAPK patch; DEX/native/assets and runtime
checks; publication for the requested device test. Existing supported targets
remain unchanged. No 5.59.0 release is ready.
