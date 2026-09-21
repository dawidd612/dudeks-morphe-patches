# Keep TikTok DM scroll position

## Scope and control

Default-enabled bytecode add-on for TikTok Global `com.zhiliaoapp.musically`, version **47.0.3**, version code **2024700030**. It can be selected alongside kveld9 patches, or independently. TikTok Lite and the Asia package are not declared compatible without separate verification.

kveld9 currently exposes individual patches and patch-time options, not a shared Piko-style settings screen. This add-on therefore uses the Morphe patch checkbox, with an English/Polish description. Deselect and rebuild to disable it. No runtime extension, reflection, extra dependency or storage of message/user data is needed. Instagram's existing settings and implementation are unchanged.

## Observed native path

Analysis of the official 47.0.3 XAPK (base APK plus `df_a_dex.apk`):

1. `MessageListSubmitListAbilityImpl` subscribes to message events. Its subscriber (`LY/AfS164S0100000_31_I1;->accept$69` in this build) obtains the updated dataset and calls the `IMScrollToBottomAbilityImpl` decision through its interface.
2. The implementation is obfuscated as `LX/16nO;->LIZ(event, List, callback, boolean)Z`. It distinguishes `OnSendMessageEvent`, `OnAddMessageEvent`, incoming list updates and navigation events. A normal pending send can return true without testing whether the user is reading older messages.
3. A true result adds a timestamp to the separate scroll-request deque. The updated list is submitted regardless of this boolean. Returning false only prevents that extra request; it does not suppress sending, dataset updates or message-event processing.
4. The same implementation already has a native latest-message check, `LIZIZ()Z`: first visible position at most zero AND the load-more ability reports that the newest page is loaded. The patch reuses this check instead of assuming list direction or estimating proximity from pixels.

These obfuscated names document the inspected artifact only; none are hardcoded in the patch.

## Fingerprints and guard

- Send/add event classes: exact diagnostic strings `OnSendMessageEvent` and `OnAddMessageEvent` in their zero-argument string methods. Infer the message field by its type.
- Scroll decision: boolean return, four parameters including `List` and boolean, calls to `FakeMessageKt.isFakeMessage` and `getMsgStatus`; additionally require both discovered event types in its instance checks. Require the native latest-position/load-more helper in the same class, distinguishing the normal conversation controller from a second similarly shaped implementation.
- Native account check: discover the helper containing the `double_tap` scene check, then its sender-checking sibling and `(J)Z` account comparison. Confirmed DEX compares a positive sender ID with the currently signed-in account ID. No account ID is stored by the patch.
- Reply detection: the message model's `getReferenceInfo()` API. No message-body parsing or dependence on video-specific content types.

The injected prefix returns false only for **send/add event + non-null message + reply reference + own sender + away from latest**. Otherwise it branches to the original first instruction. Own-sender checking also protects incoming replies, since add events may represent another person's message.

This avoids a jump-and-restore cycle and lets TikTok preserve its existing list anchor. It does not intercept global RecyclerView methods. Ordinary outgoing messages, incoming messages, initial loads and navigation events retain their original decision. There are no timers or retained views to interfere with subsequent manual scrolling.

## Reproducible validation

Upstream inspected: `kveld9/kveld-morphe-patches` commit `c862f8ca1450ecc958120b90e237487631252c5b`, release **v1.42.2**. The target matches upstream `TIKTOK_TARGET_VERSION`.

Input SHA-256:

```text
base APK: 5d7382191ac50abeae151292f8a46e6b2c5c4b6f304994823c263ccfdb4a0b4b
df_a_dex.apk: 8db8c3f8b2744f3046d1454d8124b922fc31857d1b53be95364230e5905d2d9e
kveld v1.42.2: 74473979c22ca7982e03c9cc6104a92ae1d86e9d57e7c9736aa6c438a9da4df0
```

Build with Java 21 and access to the Morphe Gradle registry:

```sh
./gradlew :patches:buildAndroid :patches:generatePatchesList --no-daemon
```

Apply the built bundle together with kveld9 using Morphe Desktop (full bytecode mode), then execute the actual emitted guard using dexlib2 from that CLI:

```sh
javac -cp morphe.jar -d /tmp/dm-test scripts/VerifyTikTokDmDex.java
java -cp morphe.jar:/tmp/dm-test VerifyTikTokDmDex patched.apk
```

The test exercises 80 combinations of event type/null, message/null, reply, sender and latest position. It interprets the injected DEX instructions and validates receivers and branch targets, stopping when control reaches the original native code. It does not simulate Android rendering or prove that no other device-specific layout movement occurs.

Validation on 2026-09-21: GitHub Actions full build succeeded, all four fingerprints resolved, and the combined application with **25 default-enabled kveld9 patches plus this patch** rebuilt successfully in FULL mode (DEX and resources, exit code 0). The resulting DEX passed all **80 guard cases**. Local Gradle could not download its distribution because of network access; the successful full build ran in the repository's authenticated Actions environment. No physical-device test was performed here.

## Phone check (PL)

1. Odśwież źródło Dudeks w Morphe. Spatchuj czystego TikToka 47.0.3 razem z wybranymi patchami kveld9 i zaznaczonym **Keep TikTok DM scroll position**.
2. Otwórz długą rozmowę, przewiń około 100 wiadomości wstecz. Odpowiedz na stary film i porównaj jego pozycję przed wysłaniem i po wysłaniu.
3. Powtórz kilka razy, także dla tekstu i zdjęcia. Sprawdź zachowanie z otwartą klawiaturą i po jej schowaniu.
4. Sprawdź ręczne przewijanie, przycisk przejścia do najnowszej wiadomości, dotknięcie cytatu oraz ponowne otwarcie rozmowy.
5. Na dole rozmowy wyślij zwykłą wiadomość i odpowiedź; normalne przewijanie powinno działać. Sprawdź też odebranie cudzej odpowiedzi podczas czytania historii.

Device behavior still requires this phone check; compilation and DEX validation are not a substitute for it.
