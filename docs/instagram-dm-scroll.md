# Keep DM scroll position

This is a Piko add-on for the official `com.instagram.android`, not Instagram Lite.
Select this patch and Piko **Add settings** in the same Morphe patching run.
The preference appears under **Piko Settings > Direct Messages** and defaults to off.
Patching without Piko settings fails explicitly. Compile-only Piko API stubs are not packaged into the app.

## Why the thread jumps

The text-send path invokes a send-completed callback with the replied-to Direct item.
The callback constructs a `Runnable` and schedules it with `View.postDelayed`.
The runnable checks that the fragment is resumed, then calls the Direct scroll controller,
which scrolls the thread's RecyclerView to adapter position 0 (the newest item).

The patch guards only that scheduling call. With the preference on, a non-null reply
item and Instagram's own at-latest predicate returning false, the forced scroll is
not scheduled. All remaining send-completion work still runs. There is no anchor
snapshot, delayed restoration, listener, reflection, or global RecyclerView change.
No message content, identifier, view or thread state is retained by the extension.

This design leaves Instagram's dataset anchoring in control. It does not promise to
prevent layout movement from keyboard changes, media resizing, deletions or pagination.

## Fingerprints and scope

- `DirectTextSendFingerprint`: the `DirectThreadFragment.sendTextMessage` and
  `DirectSendHelperImpl.sendSideChatContextualQuery` strings, with boolean return type.
- `DirectSendCompletedFingerprint`: four strings in the send-completed callback,
  four parameters ending in an integer, and a `View.postDelayed(Runnable, long)` call.
- Additional checks connect this callback to the sender's interface; resolve the
  runnable's controller constructor parameter; verify its resumed-fragment check;
  identify the immediate/smooth RecyclerView scroll paths; and resolve the
  controller's first-visible/first-completely-visible at-latest predicate.
- Reuse only the two temporary registers immediately overwritten by the original
  `int-to-long`. Reject changed instruction shapes instead of guessing registers.
- Piko settings are connected during patch finalization, after bundles have merged
  their extensions. Its preference widget and shared preference storage are reused.

Obfuscated names below are audit evidence only, never matching criteria in the patch.

## Analysis input

- Piko upstream: `50744aa07bb41c4e1f942a06614ef4e6f2e3610c`, release `v3.9.0`.
- Upstream target: Instagram `439.0.0.37.89`, ARM64, version code `384510827`, APKM.
- Available analysis input: APKPure `439.0.0.37.89`, ARM64, code `384510833`, XAPK.
- Base APK SHA-256: `37501d681e64fb20a378aad94668668db323f0e55f3086bbd5ca73321f534376`.

These are different variants. Analysis of `384510833` is not verification of `384510827`.
The latter remains the compatibility target to match Piko. Exact-variant verification
and physical-device behavior must be confirmed before treating this as a stable release.

Observed chain in the analysis input:

1. `X/03Yg.A08(...)Z`: resolves the reply item through the reply context and invokes
   `X/0Hkp.APW(..., DirectItem, int)` after sending.
2. `X/03NH.APW(...)V`: at byte offsets `0x005a` through `0x0066`, constructs
   `X/0EkJ` with `X/03NF` and calls `View.postDelayed`.
3. `X/0EkJ.run()`: checks `Fragment.isResumed()` and calls `X/03NF.A01(false)`.
4. `X/03NF.A01(boolean)`: selects the immediate/smooth scroll-to-position-0 path.
5. `X/03NF.A02()Z`: checks first completely visible position, falling back to first
   visible position; position 0 means at latest.

Other callers of the scroll controller include click handlers, thread opening and
dataset updates. They are not modified. The normal update path in `X/04Wv.A00`
already captures the at-latest predicate before updating the dataset.

## Phone test

### Automated checks completed

- Full `:patches:buildAndroid` build in GitHub Actions: passed (Kotlin, Java and extension DEX).
- `python3 scripts/test_instagram_dm_hook.py`: passed. Tests the production Java hook
  with small Android/Piko fakes: default off, eight combinations of reply/location/toggle,
  unavailable preferences, null settings arguments and duplicate preference insertion.
- Morphe Desktop `1.16.0`, Piko `3.9.0`, analyzed XAPK `384510833`: both **Add settings**
  and **Keep DM scroll position** applied successfully; full DEX and resource rebuild passed.
  This developer test used `--force` because the input variant differs from Piko's target.
- Applying the add-on without Piko: rejected at finalization with the explicit missing
  **Add settings** message; no output APK produced.
- Decoded output confirms the conditional skips only the send callback's `postDelayed`.
  The Direct scroll controller and delayed runnable are instruction-for-instruction
  unchanged. The extension includes only its hook and generated `R` class, not Piko stubs.
- No physical-device test or ART runtime verification has been performed.

### On a phone

Use a build from this branch's successful **Verify Instagram add-on** Actions run.
Extract the `dudeks-patches-instagram` artifact ZIP and import its `.mpp` into Morphe.
Keep the original app's signing-key requirements in mind when replacing a previous patched installation.

1. Choose a clean supported Instagram APKM. Select this patch and Piko **Add settings**
   together (multiple sources may require Morphe expert mode).
2. Enable **Keep scroll position when replying** under Piko's Direct Messages settings.
   If Piko asks to restart, restart the app.
3. Scroll roughly 100 messages up, reply to an old Reel, and send. Check that the
   Reel remains around the same visible position after sending and after delivery.
4. Repeat for text, photo and shared-post replies; send several replies in sequence.
5. At the bottom, send a plain message and a reply. Both should follow new messages normally.
6. Scroll manually during delivery, tap jump-to-latest, open a search result, switch
   conversations, leave/reopen the app, and receive a new message while reading history.
   Explicit navigation must still work. No other conversation should inherit state.
7. Turn the preference off and repeat the old-Reel case to compare stock behavior.

Automated metadata is generated by the existing release workflow. Do not edit
`patches-list.json`, the release index or `PATCHES.md` by hand.
