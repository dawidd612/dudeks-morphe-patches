## [1.33.9](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.8...v1.33.9) (2026-10-04)

### 🐛 Bug Fixes

* **calendar:** promote confirmed Calendar patches to stable ([c6e06c3](https://github.com/dawidd612/dudeks-morphe-patches/commit/c6e06c3a35443cc462b2042d6322ecfa813f2500))
* **calendar:** promote device-confirmed Calendar patches to stable ([642a383](https://github.com/dawidd612/dudeks-morphe-patches/commit/642a383c6e49c5e4261af8232f6100fc94249197))
* **calendar:** publish device-confirmed stable Calendar patches ([75848e3](https://github.com/dawidd612/dudeks-morphe-patches/commit/75848e35a017975a5ca0c7fd6582bd074c5f1d5c))

## [1.33.8](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.7...v1.33.8) (2026-10-04)

### 🐛 Bug Fixes

* **calendar:** correct OAuth identity after signing-key rotation ([431c968](https://github.com/dawidd612/dudeks-morphe-patches/commit/431c968f7084d3d756f2b3f83bd5d2843e4a9d51))
* **calendar:** publish OAuth signing-key rotation correction ([8a31617](https://github.com/dawidd612/dudeks-morphe-patches/commit/8a316170c00fc4603cfafbf33b01e1de55802ed4))
* **calendar:** use pre-rotation OAuth signer instead of rejected Android 13 certificate ([2ce06d0](https://github.com/dawidd612/dudeks-morphe-patches/commit/2ce06d0152e8d71d457cc42b8323002c5a2506b9))

## [1.33.7](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.6...v1.33.7) (2026-10-03)

### 🐛 Bug Fixes

* **calendar:** release genuine MicroG consent and sign-in routing ([9fda77e](https://github.com/dawidd612/dudeks-morphe-patches/commit/9fda77ed13a846592f45f3ee1e30266cc1aa9043))
* **calendar:** request MicroG Calendar consent and redirect native sign-in ([eb49ab0](https://github.com/dawidd612/dudeks-morphe-patches/commit/eb49ab031e181601020a5437aac948b510244703))
* **calendar:** route sign-in to genuine MicroG consent ([bbcd8a3](https://github.com/dawidd612/dudeks-morphe-patches/commit/bbcd8a3da16b0b733fdabd54e9265f4b5623d2e4))

## [1.33.6](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.5...v1.33.6) (2026-10-03)

### 🐛 Bug Fixes

* **calendar:** add optional MicroG-RE synchronization authentication ([a9f573e](https://github.com/dawidd612/dudeks-morphe-patches/commit/a9f573e75ebe1a70cd6e8f0cff703d68692e7576))
* **calendar:** add optional MicroG-RE token transport for re-signed installs ([929ac5f](https://github.com/dawidd612/dudeks-morphe-patches/commit/929ac5fa760e255ed4a6981c45c8164a60694546))
* **calendar:** release MicroG-RE synchronization token transport ([8541a86](https://github.com/dawidd612/dudeks-morphe-patches/commit/8541a86550470f56b20043835d1d638fdca2b112))

## [1.33.5](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.4...v1.33.5) (2026-10-03)

### 🐛 Bug Fixes

* **calendar:** release existing account visibility support ([10d275c](https://github.com/dawidd612/dudeks-morphe-patches/commit/10d275c4268651bfd1c0dfcf9386c89fdce8ea4c))
* **calendar:** request existing Google account visibility through Android chooser ([f76ab18](https://github.com/dawidd612/dudeks-morphe-patches/commit/f76ab1846a8d9185f813cbc79160a17c3df133c6))
* **calendar:** request visibility for an existing Google account ([56eda47](https://github.com/dawidd612/dudeks-morphe-patches/commit/56eda472cb653c646db87852b8d857730c56b8a0))

## [1.33.4](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.3...v1.33.4) (2026-10-03)

### 🐛 Bug Fixes

* **calendar:** allow independent re-signed fresh installations ([e5da00f](https://github.com/dawidd612/dudeks-morphe-patches/commit/e5da00fc1cb84bd77b5e3cfd7c7d12d509f926c7))
* **calendar:** handle namespace-unaware Morphe manifest documents ([1e52333](https://github.com/dawidd612/dudeks-morphe-patches/commit/1e52333cd8fde2d94f300cde5b5d057f713670bf))
* **calendar:** release shared UID installation compatibility ([ff1ff82](https://github.com/dawidd612/dudeks-morphe-patches/commit/ff1ff82b18a6403a88a718419496c95ae9b23692))
* **calendar:** remove Google shared UID from re-signed fresh installs ([b0b52c8](https://github.com/dawidd612/dudeks-morphe-patches/commit/b0b52c81d3ba2f7188b15bb500c9f8af7bfca24a))

## [1.33.3](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.2...v1.33.3) (2026-10-02)

### 🐛 Bug Fixes

* **calendar:** compare decoded corner dimensions semantically ([b84b79e](https://github.com/dawidd612/dudeks-morphe-patches/commit/b84b79e01c0b5a5714d6b86b8689e57f29e49ac4))
* **calendar:** release Morphe resource compatibility hotfix ([f80e3da](https://github.com/dawidd612/dudeks-morphe-patches/commit/f80e3dad60cf5c438f58d7c74c766b0da8db7b86))
* **calendar:** ship compiled nine-patch PNGs for Morphe resource encoding ([ada1a9e](https://github.com/dawidd612/dudeks-morphe-patches/commit/ada1a9e0d2ab6261a57d14d3feb05ceb6d101069))
* **calendar:** support Morphe dimensions and compiled tile resources ([7a9b316](https://github.com/dawidd612/dudeks-morphe-patches/commit/7a9b31672c5d95ddbd2c4b75c00edfddda7b3c7c))

## [1.33.2](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.1...v1.33.2) (2026-10-02)

### 🐛 Bug Fixes

* **calendar:** preserve schedule tile geometry after delayed corruption ([ca11861](https://github.com/dawidd612/dudeks-morphe-patches/commit/ca11861009dca2f08c60d5407d8eca836bf59e8c))
* publish Calendar tile repair and stable Pizza/Coffee targets ([3e3e286](https://github.com/dawidd612/dudeks-morphe-patches/commit/3e3e2869de46803929b2058a358c11acef3bd989))
* stabilize Calendar tile geometry and mark Pizza/Coffee stable ([0093a73](https://github.com/dawidd612/dudeks-morphe-patches/commit/0093a7381f7dac28bdd19bc42a8aebd64bb5af8d))
* **tapblaze:** mark supported Pizza and Coffee patches stable ([aa26b58](https://github.com/dawidd612/dudeks-morphe-patches/commit/aa26b58e70d63b2df66e5f7ee016bd897029787d))

## [1.33.1](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.33.0...v1.33.1) (2026-09-30)

### 🐛 Bug Fixes

* **coffeebusiness:** restore Singular network callback after restart ([d540f54](https://github.com/dawidd612/dudeks-morphe-patches/commit/d540f5441884243324012576f4df5b53c83190f7))

## [1.33.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.32.0...v1.33.0) (2026-09-30)

### ✨ New Features

* **coffeebusiness:** add experimental startup, rewards and MicroG patches ([529f096](https://github.com/dawidd612/dudeks-morphe-patches/commit/529f0965957810635214f649317a6ee2befb52f9))

## [1.32.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.31.1...v1.32.0) (2026-09-28)

### ✨ New Features

* **pizzabusiness:** hide paid offers and cancel unsupported checkout ([1bd0c2b](https://github.com/dawidd612/dudeks-morphe-patches/commit/1bd0c2b3a6f53804203b2b313294ce857158ccb9))

## [1.31.1](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.31.0...v1.31.1) (2026-09-28)

### 🐛 Bug Fixes

* **pizzabusiness:** stabilize startup and Google Play Games lifecycle ([de4d708](https://github.com/dawidd612/dudeks-morphe-patches/commit/de4d708fec03c9bdb638e35b85954985edee49c3))

## [1.31.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.30.1...v1.31.0) (2026-09-27)

### ✨ New Features

* **pizzabusiness:** release licensing repair and MicroG Games integration ([ddb4879](https://github.com/dawidd612/dudeks-morphe-patches/commit/ddb487930d3f227c4696c8fadc582dccf3dae0bd))
* **pizzabusiness:** repair Play licensing flow and add MicroG Games support ([c37d208](https://github.com/dawidd612/dudeks-morphe-patches/commit/c37d2080ca9d55dc7f1cd6678be3a0da01d8e5c6))

## [1.30.1](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.30.0...v1.30.1) (2026-09-27)

### 🐛 Bug Fixes

* **release:** checksum the final bundle after Gradle publish ([a4d4bd9](https://github.com/dawidd612/dudeks-morphe-patches/commit/a4d4bd90fd5cd5bc27ac3d23a2200a796416c27c))
* **release:** publish corrected bundle checksums ([6f44aeb](https://github.com/dawidd612/dudeks-morphe-patches/commit/6f44aebbee9cfbaf13f633714de7957450992816))

## [1.30.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.29.4...v1.30.0) (2026-09-27)

### ✨ New Features

* **pizzabusiness:** publish rewarded video skip ([#13](https://github.com/dawidd612/dudeks-morphe-patches/issues/13)) ([031d4c3](https://github.com/dawidd612/dudeks-morphe-patches/commit/031d4c35bf96d4c184bfde0b17bf743ec9c8cb4c))
* **pizzabusiness:** skip rewarded videos through the native reward flow ([#12](https://github.com/dawidd612/dudeks-morphe-patches/issues/12)) ([ded73f2](https://github.com/dawidd612/dudeks-morphe-patches/commit/ded73f2e523c779f6c7b50129d00a3f8cb6bc8dc))

## [1.29.4](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.29.3...v1.29.4) (2026-09-27)

### 🐛 Bug Fixes

* **calendar:** publish schedule widget stabilization ([#11](https://github.com/dawidd612/dudeks-morphe-patches/issues/11)) ([6704e49](https://github.com/dawidd612/dudeks-morphe-patches/commit/6704e4911e0a76e18cf3bff85f56c9ffb89b9fbd))
* **calendar:** stabilize schedule widget rows on Android 16 ([#10](https://github.com/dawidd612/dudeks-morphe-patches/issues/10)) ([a25be15](https://github.com/dawidd612/dudeks-morphe-patches/commit/a25be1560329fd15fec1c7212045def8c47fd485))

## [1.29.3](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.29.2...v1.29.3) (2026-09-24)

### 🐛 Bug Fixes

* **gardenscapes:** expose silent Play Games sign-in failures ([#9](https://github.com/dawidd612/dudeks-morphe-patches/issues/9)) ([c748a65](https://github.com/dawidd612/dudeks-morphe-patches/commit/c748a650faaa52d21800af279c33f2771f6ddbf4))

## [1.29.2](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.29.1...v1.29.2) (2026-09-22)

### 🐛 Bug Fixes

* **gardenscapes:** repair saved stars when opening the garden ([#8](https://github.com/dawidd612/dudeks-morphe-patches/issues/8)) ([3ddcb5d](https://github.com/dawidd612/dudeks-morphe-patches/commit/3ddcb5da0d280f8386a0e3618fb7d9a358361360))

## [1.29.1](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.29.0...v1.29.1) (2026-09-22)

### 🐛 Bug Fixes

* **gardenscapes:** skip local installation dialog and enable repair by default ([#7](https://github.com/dawidd612/dudeks-morphe-patches/issues/7)) ([be1494a](https://github.com/dawidd612/dudeks-morphe-patches/commit/be1494ab43028814a5d06e34783ca8e51725c8f9))

## [1.29.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.28.1...v1.29.0) (2026-09-22)

### ✨ New Features

* **gardenscapes:** add experimental one-time negative star repair ([#6](https://github.com/dawidd612/dudeks-morphe-patches/issues/6)) ([fe0990d](https://github.com/dawidd612/dudeks-morphe-patches/commit/fe0990d26507fe50bd7224e83f72f698bad22ac8))

## [1.28.1](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.28.0...v1.28.1) (2026-09-21)

### 🐛 Bug Fixes

* **tiktok:** preserve history for all messages and composer resizes ([#5](https://github.com/dawidd612/dudeks-morphe-patches/issues/5)) ([8cf1415](https://github.com/dawidd612/dudeks-morphe-patches/commit/8cf1415c596d7ef2ff3483a1431d76c71295a3af))

## [1.28.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.27.2...v1.28.0) (2026-09-21)

### ✨ New Features

* **tiktok:** keep DM scroll position with kveld9 patches ([#4](https://github.com/dawidd612/dudeks-morphe-patches/issues/4)) ([0bc3667](https://github.com/dawidd612/dudeks-morphe-patches/commit/0bc36679ef30220e26cc31278bce468b82a2059c))

## [1.27.2](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.27.1...v1.27.2) (2026-09-21)

### 🐛 Bug Fixes

* **instagram:** prevent reply viewport nudges and preserve the animated anchor ([bf40f0c](https://github.com/dawidd612/dudeks-morphe-patches/commit/bf40f0c0c05821ce72f4d40b1f4239b7426a8c68))

## [1.27.1](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.27.0...v1.27.1) (2026-09-20)

### 🐛 Bug Fixes

* **instagram:** preserve reply resize offset and enable Polish setting by default ([fbeddcf](https://github.com/dawidd612/dudeks-morphe-patches/commit/fbeddcfafc9b152b4644081ad9ea5cba3f3a86df))

## [1.27.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.26.0...v1.27.0) (2026-09-20)

### ✨ New Features

* **instagram:** keep DM scroll position when replying ([92de396](https://github.com/dawidd612/dudeks-morphe-patches/commit/92de396ac6eb88e75ebff4da20ed6ca022b05a2d))

## [1.26.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.25.0...v1.26.0) (2026-09-19)

### ✨ New Features

* **hikingmap:** hide Premium offers and trial notifications ([c35c361](https://github.com/dawidd612/dudeks-morphe-patches/commit/c35c36107762942799923a68a7919c1c2b55fea4))

## [1.25.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.24.0...v1.25.0) (2026-09-19)

### ✨ New Features

* **hikingmap:** add opt-in local Premium patch for 1.16.6 ([74afd22](https://github.com/dawidd612/dudeks-morphe-patches/commit/74afd22cf632fbc33eb8601e89fb141a63d14742))

## [1.24.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.23.0...v1.24.0) (2026-09-19)

### ✨ New Features

* **andropods:** split Premium and Play Store patches ([39847c2](https://github.com/dawidd612/dudeks-morphe-patches/commit/39847c2494d610fbeed66b64fb0cc85025de337c))

## [1.23.0](https://github.com/dawidd612/dudeks-morphe-patches/compare/v1.22.3...v1.23.0) (2026-09-17)

### ✨ New Features

* **repo:** focus source on AndroPods only ([ca5b2f1](https://github.com/dawidd612/dudeks-morphe-patches/commit/ca5b2f18574a4f37644f39b0b6f8a5dfb760ed91))

# Changelog

## 1.22.3 - 2026-09-17

- Added dedicated AndroPods application metadata for Morphe.

## 1.22.2 - 2026-09-17

- Disabled the PairIP Google Play ownership screen and its fallback shutdown paths.

## 1.22.1 - 2026-09-17

- Reworked the Pro-state patch for AndroPods 1.5.30 to resolve the obfuscated premium field dynamically.
