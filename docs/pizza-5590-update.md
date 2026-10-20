# Pizza5.59.0 (2322) validation checkpoint

The port now has an active experimental ARM64 profile. Official publication is
pending completion of account, restart, clean-install and save-persistence tests.

Publisher-signed APKPure XAPK SHA256:
085a7462ec672c22bebbefc0d79a0480ae1d68b72cb3ad7f6b5ecc501b39b99d.
All five embedded APK signatures and CRCs passed the initial audit. The original
input remains pinned, not replaced by another store build.

CI capture39280529797 (source820ab6c) supplies two independent original-signed
starts with1477 identical strings in32 holders. Native reconstruction changes
50923 executable bytes,3798 constant qwords and200 imports. The only reviewed
IFUNC is memmove in the identical previously reviewed system libc; no runtime
address is embedded. Native input c97e83fd73d3c871d26d2f2587c1f9c99279505a5a70f6787220572c958afe26
reconstructs to b54a7104f95b6ca89f5fb59ac5250a2ffde40922446b505c61f14f211ed36135.
Two visibility instruction windows match uniquely after the reconstructed layout
moves them by0x1000. The separate visibility delta outputs
94bcdf5583791deb65d921d017c2303ba2cb74f5e85988ed6e3737dbf7ffff19.
Six matching upstream SDK methods are restored with their actual behavior.

Local source build and all three patches apply to the complete clean input.
DEX checks pass1477 string initializers,3418 unchanged auth/SDK methods,2165
unchanged billing methods,87 unchanged native bridge methods,6 recovered SDK
methods and5 interpreted reward placements. Play-services guards preserve23
other methods and cancel the two update-code dialogs once. All5 input APKs,
17 native libraries and6772 assets pass preservation checks. Native corruption,
reapply, malformed-range and exact reconstruction checks pass.

Disposable local API35 upgrade retains Chapter1/Day2/funds64.38/gems10. Actual
PNN reward gives exactly one gem(10->11) and one native GLThread66 sequence under
double tap/Home/resume. Three checkout invocations cancel on GLThread with zero
Billing entry/success callbacks. A pepperoni order is prepared, baked, cut and
served; no game-binary behavior is altered by the test driver.

C: had insufficient free space for emulator startup. Test AVD data is copied to
E:/Codex-Morphe-Pizza-5590-20261020; original AVDs and signing key are retained.
Before upgrading the debug copy, its113-entry files/preferences backup was read
and verified in full. Logs, APKs, captures, account data and saves stay local,
outside Git. Repeat-start/reboot/offline/clean-install and actual account matrix
are in progress. See pizza-5590-provenance.json for exact reconstruction evidence.
