# Dudek's Morphe Patches

[![Release](https://img.shields.io/github/v/release/dawidd612/dudeks-morphe-patches)](https://github.com/dawidd612/dudeks-morphe-patches/releases/latest)
[![Build](https://img.shields.io/github/actions/workflow/status/dawidd612/dudeks-morphe-patches/release.yml?label=build)](https://github.com/dawidd612/dudeks-morphe-patches/actions/workflows/release.yml)
[![License](https://img.shields.io/github/license/dawidd612/dudeks-morphe-patches)](LICENSE)

Small, focused patch source for AndroPods. There are no unrelated app patches in this repository.

## Add to Morphe

[Add Dudek's Morphe Patches](https://morphe.software/add-source?github=dawidd612/dudeks-morphe-patches)

If the button does not open Morphe, add this repository manually:

```text
https://github.com/dawidd612/dudeks-morphe-patches
```

## Supported app

<!-- PATCHES_START -->
> **[v1.23.0](https://github.com/dawidd612/dudeks-morphe-patches/releases/tag/v1.23.0)**&nbsp;&nbsp;&middot;&nbsp;&nbsp;`main`&nbsp;&nbsp;&middot;&nbsp;&nbsp;**1 patch** across **1 app**&nbsp;&nbsp;&middot;&nbsp;&nbsp;[Full details](PATCHES.md)

| # | App | Patches | Version | Package |
|---|---|---|---|---|
| 1 | [**AndroPods (Dudek Fix)**](PATCHES.md#andropods-dudek-fix-provitaliiandropods) | 1 | `1.5.30` | [`pro.vitalii.andropods`](https://play.google.com/store/apps/details?id=pro.vitalii.andropods) |
<!-- PATCHES_END -->

Use the clean AndroPods `1.5.30` XAPK with version code `86`. Other releases are not marked as compatible.

## What the patch fixes

`AndroPods Pro + Play Fix` handles two separate checks in the app:

- it keeps the local Pro state enabled after the billing state is refreshed;
- it disables the PairIP installer and ownership flow that shows the "Get this app from Play" screen after Morphe signs the rebuilt package.

The premium field is resolved from AndroPods' own purchase-result method instead of relying on an obfuscated class or field name. This makes the patch less fragile when those names differ between builds.

## Updating an existing install

Refresh this source in Morphe, patch a clean copy of the supported XAPK and install the newly generated package. An APK created with an older patch release will not update itself.

## Problems

Open an [issue](https://github.com/dawidd612/dudeks-morphe-patches/issues) and include:

- the AndroPods version and version code;
- where the clean XAPK came from;
- the Morphe version and this source's release number;
- the complete patching log;
- a screenshot or short description of the runtime problem.

## Building

The project uses Java 21 and the Morphe patch Gradle plugin.

```shell
./gradlew :patches:buildAndroid --no-daemon
```

Releases are built by GitHub Actions and include the `.mpp` bundle, patch metadata and SHA-256 checksums.

## License and attribution

The repository is licensed under [GPL-3.0](LICENSE). The patch infrastructure originated from the Morphe patch template and earlier GPL-licensed community work; retained notices remain in the relevant files. This project is maintained separately and is not affiliated with the AndroPods developer or the Morphe project.
