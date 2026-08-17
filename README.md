# Voice Assistant — Phase 1

Personal AI Voice Assistant ka Phase 1 starter project.

## Is Phase mein kya hai
- Microphone permission handling (runtime request)
- Speech-to-Text (Android `SpeechRecognizer`, `hi-IN` language)
- Text-to-Speech response
- Simple Jetpack Compose UI: mic button (pulse animation jab sun raha ho), current exchange, aur command history
- `VoiceAssistantViewModel.handleCommand()` mein ek placeholder hook — yahi function Phase 2 mein Action Manager (app launching, WhatsApp, calling) se replace hoga

## Setup
1. Android Studio (Koala ya newer) mein `File > Open` se ye folder kholo.
2. Gradle sync hone do (pehli baar internet chahiye hoga dependencies download karne ke liye).
3. Ek real device par run karo (emulator par speech recognition kaam nahi karta — Google app/mic access nahi hota).
4. App open hote hi mic permission maangega — allow karo.
5. Mic button dabao, kuch bolo (Hindi/English/Hinglish), assistant text mein dikhayega aur bolke bhi bolega.

## Known limitations (jaan-boojh kar, Phase 1 ke liye)
- Koi command actually execute nahi hoti abhi — sirf "maine suna: ..." wapas bolta hai. Ye intentional hai taaki pehle mic → STT → TTS ka poora loop stable ho jaaye.
- Wake-word nahi hai abhi (manual tap se start karna hoga) — Phase 6 mein add hoga.
- App launcher icon placeholder (solid color) hai — polish baad mein.

## Next steps (Phase 2)
- `AppLauncher` module: Intent-based app opening ("YouTube kholo" → `PackageManager` se launch intent)
- Simple keyword-based command parser (`handleCommand()` ke andar) — baad mein AI/LLM-based samajh se upgrade hoga
