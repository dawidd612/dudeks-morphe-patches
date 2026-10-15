# PNN sponsor sequence: 1000 offers (5.58.0 ARM64)

The original NewsLayer has two placements rather than a numeric two-ad cap:
empty -> PNN -> PNN2. PNN completion leaves the sponsor controls available;
PNN2's ended callback schedules the existing broadcast close. Changing only
Java rewarded readiness cannot extend that sequence.

`1000 PNN sponsor rewards` repeats PNN for 999 successful completions, then
selects PNN2 for the final, 1000th offer. The first empty -> PNN request,
reward amounts, original reward callback, skip controls and PNN2 close path
are unchanged. It works with or without `Skip rewarded ads`; that patch is
needed to omit the videos. No ad-network impression/postback is generated
by this new patch. Other placements and paid purchases are not changed.

The per-layer counter is an added uint32 at +0x520. The sole NewsLayer
allocation and its memset grow from 0x520 to 0x528; all three sized-delete
sites receive the same size. The single direct constructor caller is
0x135ea44 -> 0x135eb54. The counter is separate from the original saved
TODAY_PNN_WATCHED statistic, which keeps its normal increment/reset logic.
A fresh NewsLayer has a fresh counter, even within the same real-world day.
This is a sequence limit, not a new persistent quota across force-stops.

Exact clean XAPK SHA256:
`6f4c6f9faa13265d0533aa61c8b294938cbfbf439e971ba162fb5e51d3b8dc6b`.
The transform accepts only the exact restored 5.58.0 library hash or the
exact optional funds-store output hash. It runs in the resource finalizer
so that both optional-patch combinations work. Unknown/already-modified
native inputs fail before a file is written. 5.57.3 is not advertised.

The 64-byte assembly payload adds a read/execute PT_LOAD at VA 0x23e0000,
using reserved program-header space. All segments retain 16 KiB offset/VA
congruence. It contains no external calls, stack frames, persistent data
or writable executable memory. Only the allocation sizes, two hook
instructions and ELF program-header metadata change in the existing file.

Rebuild and execute the payload:

```sh
python -m pip install keystone-engine==0.9.2 unicorn==2.1.4 pyelftools
python scripts/build_pizza_pnn_payload.py
python scripts/test_pizza_pnn_arm64.py
```

For actual native output, compile `VerifyPizzaPnnNative.java` against the
compiled patch classes and Kotlin stdlib, run it with
`RESTORED.so pnn-1000-arm64.b64 OUTPUT.so`, then use
`python scripts/test_pizza_pnn_arm64.py --library OUTPUT.so`.
Repeat for both bootstrap-only and funds-store inputs.

ARM64 instruction tests cover the shipped binary, all 999 successful PNN
callbacks, PNN2 selection at the boundary, failed/cancelled attempts, fresh
layer reset, displaced saved-counter arithmetic and unchanged original
members/callee-saved registers. They do not execute the Android game UI.

This session has no ADB/device/emulator. Full gameplay, normal close/skip,
third and later rewards, 1000th completion, lifecycle, authentication and
saved-progress regression checks must still run on a disposable/recoverable
test installation before a stable release. A compiled candidate is not a
runtime-verified release. Keep the user's existing save and signing key.
