# Dudek's Morphe Patches

[![Release](https://img.shields.io/github/v/release/dawidd612/dudeks-morphe-patches)](https://github.com/dawidd612/dudeks-morphe-patches/releases/latest)
[![Build](https://img.shields.io/github/actions/workflow/status/dawidd612/dudeks-morphe-patches/release.yml?label=build)](https://github.com/dawidd612/dudeks-morphe-patches/actions/workflows/release.yml)
[![License](https://img.shields.io/github/license/dawidd612/dudeks-morphe-patches)](LICENSE)

My collection of Android app patches for [Morphe](https://morphe.software). Each patch can be selected separately; supported versions are listed below.

## Installation

[Add this source to Morphe](https://morphe.software/add-source?github=dawidd612/dudeks-morphe-patches), or add the repository manually:

```text
https://github.com/dawidd612/dudeks-morphe-patches
```

Choose a supported app version, select the patches you want and patch a clean APK or XAPK. After a source update, rebuild the app to apply the changes.

## Patches

<!-- PATCHES_START -->
See [PATCHES.md](PATCHES.md) for the generated patch list.
<!-- PATCHES_END -->

### AndroPods

Use version **1.5.30**, version code **86**, in XAPK format.

| Patch | What it does |
| --- | --- |
| Premium | Enables Pro features and keeps them enabled when billing refreshes. |
| Play Store Fix | Removes the startup check that asks you to install the app from Google Play. |

Both are selected by default. Play Store Fix also works without Premium. Premium alone does not remove the installation check.

### Android blocks overlay access

If Android displays "App was denied access" when you enable "Display over other apps", open **Settings > Apps > AndroPods > More (three dots) > Allow restricted settings**. Confirm the prompt, then return to the overlay setting and enable it.

Only allow this for an APK you trust. The restriction belongs to Android, so neither patch grants the permission or removes the system prompt. Menu names vary by device; see [Google's instructions](https://support.google.com/android/answer/12623953). If the option is missing, include your phone model and Android version in a bug report.

## Bugs and contributions

[Open an issue](https://github.com/dawidd612/dudeks-morphe-patches/issues) with the app version, Android and Morphe versions, source release, selected patches and logs. For crashes, include a crash log if you can.

New patches and fixes are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Build

Requires Java 21 and access to the Morphe Gradle dependencies.

```shell
./gradlew :patches:buildAndroid --no-daemon
```

[Releases](https://github.com/dawidd612/dudeks-morphe-patches/releases) contain the `.mpp` bundle, metadata and SHA-256 checksums. GitHub Actions builds and publishes them from `main`.

## Credits and license

Based on [rushiranpise/morphe-patches](https://github.com/rushiranpise/morphe-patches) and the Morphe patch tooling. Licensed under [GPL-3.0](LICENSE); see [NOTICE](NOTICE) for retained terms. Maintained independently of Morphe and the patched app developers.
