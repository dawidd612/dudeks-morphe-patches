# Gardenscapes negative-star repair (experimental)

Target: original Gardenscapes 9.9.0, com.playrix.gardenscapes, ARM64.
The native library must have SHA-256
`3a15c3c170c21a12b5f0253a9421b6bc41707f4e34285acbbcae8afde4851c5c`.
Other builds, other CPU architectures and previously modified libraries are unsupported.

The supplied XAPK contains the game engine in libgame.so and PLXE-encrypted Lua/XML
assets. DEX hooks cannot directly repair the engine's saved star balance.
ARM64 disassembly confirms the Lua GetStars binding at 0x3c0f930, its wrapper at
0x3c15ad8, and the earned/spent property offsets 912/1016. The property reader is
0x38b6d08. The earned-star transaction at 0x38bd6e4 reads the earned property,
adds its argument, calls the property setter at 0x3871490 and notifies listeners.
The cached UI fields alone are not patched.

A strictly hash-guarded raw-resource patch adds a read/execute ELF segment,
relocates the program-header table into it, and redirects the transaction entry.
The original prologue is replayed by a trampoline. The native helper calls the
original transaction with `spent + 2 - earned` only for a negative balance and a
positive reward. Normal balances and non-positive transactions remain unchanged.
The final balance is 2, inclusive of that reward. Integer overflow fails closed.

An app-private marker under files/.dudek-stars-repaired-v1 is protected with flock.
It is marked complete only after rereading a balance of 2. Marker presence prevents
another correction even if a later balance becomes negative. Empty files from an
interrupted operation are retryable; a repaired positive balance is never topped up.
Paths derive the Android user number from UID and support secondary users. Storage
or locking errors preserve the original reward. Clearing app data or reinstalling
removes the marker. No account information or message content is stored.

This does not bypass server synchronization or integrity checks. Static/native logic
checks do not prove that a cloud save will accept the correction. Actual device
behavior, restart persistence and cloud synchronization require testing. The patch
is experimental and unselected by default. Do not claim confirmed recovery yet.

## Building

`python3 scripts/build_gardenscapes_payload.py` runs host tests against the production
C helper and builds the freestanding ARM64 payload with Clang/LLD. Only our code is
included in the payload. Native game offsets are locked to the full library hash.
`python3 scripts/test_gardenscapes_arm64.py` executes the compiled payload and
trampoline with Unicorn at two load addresses, checking registers, stack, marker
persistence and error paths.
`python3 scripts/verify_gardenscapes_apk.py original.xapk patched.apk` verifies
the emitted APK, its unchanged DEX and the exact ELF changes.
The build rejects a mismatch with the committed payload. After deliberately changing
the helper or compiler, use `--update`, review the new bytes and rerun all tests.
The generated base64 payload belongs in patches/src/main/resources/gardenscapes.

## Test na telefonie

Zachowaj dotychczasowy postęp. Nie odinstalowuj gry ani nie czyść danych tylko po to,
żeby wgrać patch; jeśli podpis instalacji jest inny, najpierw rozwiąż kwestię kopii
zapisu. Sam XAPK nie zawiera Twojego postępu.

1. Spatchuj oryginalne 9.9.0 w Morphe, wybierając Repair negative stars once.
2. Na urządzeniu ARM64 otwórz zapis z ujemnym saldem i zdobądź jedną gwiazdkę.
3. Oczekiwany wynik: 2 gwiazdki. Wydaj jedną w ogrodzie i sprawdź rzeczywiste
   wykonanie zadania, a nie tylko licznik.
4. Uruchom grę ponownie i sprawdź saldo. Zdobądź kolejną gwiazdkę: ma przybyć
   normalnie jedna, bez ponownego ustawiania na 2.
5. Osobno sprawdź zachowanie po synchronizacji zapisu. Cofnięcie salda przez serwer
   oznacza, że lokalna naprawa nie rozwiązała problemu synchronizacji.

## Validation performed

- Full Kotlin/Java patch and extension build passed.
- 19 host scenarios execute the production C helper.
- 16 scenarios execute the compiled ARM64 payload and trampoline, at two load
  addresses, preserving stack and callee-saved registers.
- Morphe Desktop 1.16.0 successfully patched and rebuilt the supplied 9.9.0 XAPK.
- The emitted APK verifier passed: original DEX unchanged, one native entry branch,
  exact compiled payload, preserved original ELF segments and a correctly relocated
  program-header table with a read/execute payload segment.
- Phone launch, actual garden spending and cloud/restart persistence remain untested.
