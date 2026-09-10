# Sort Escape

A casual sorting/puzzle game for Android, built with [libGDX](https://libgdx.com/) (Kotlin) + Box2D,
implementing the MVP scope from `Sort Escape — Complete Game Project Documentation.pdf`.

## Why libGDX instead of Unity

The design doc specifies Unity, but this machine has no Unity Editor installed (and no way to
install one interactively). libGDX was chosen instead because it satisfies the same requirements
the doc actually cares about - real 2D physics, a real renderer, Android deployment - while being
100% text/code (no editor-authored scenes or binary project files), so it can be built entirely
through an agentic coding workflow:

- **Real physics** (section 25's "excellent physics"): the board is a genuine Box2D `World`
  (gravity, restitution, impulses). Selecting-and-sorting an object applies a steering force that
  flies it into its container under simulation, not a scripted tween; a wrong sort applies a real
  impulse (see `PhysicsWorld.kt`).
- **Graphics without an art budget** (section 22 "Premium Minimal Casual"): every texture -
  rounded panels, gradients, soft shadows, particles - is generated at runtime from `Pixmap`s in
  `ProceduralArt.kt`. There is no missing-art blocker for adding new object packs (section 67).
- **Buildable from the command line**: `./gradlew :android:assembleDebug` produces an APK using
  only the Android SDK + JDK 17 already on this machine, no GUI required.

## Module layout

```
core/      - platform-independent game logic + Scene2D UI (this is most of the game)
android/   - thin Android launcher wiring real audio/haptics/ads into the core interfaces
store-assets/ - Play Store listing assets (icon, feature graphic, screenshots)
```

Within `core`, package layout mirrors the doc's section 38 "Core Classes":

| Package      | Covers |
|--------------|--------|
| `data`       | Category/ObjectData/ContainerData/LevelData (sections 6, 40-42) |
| `level`      | LevelGenerator + LevelValidator + LevelRepository (sections 10-12) |
| `physics`    | Box2D world wrapper |
| `gameplay`   | SortManager - the core tap-to-sort loop (sections 4-5, 8) |
| `score`      | ScoreManager - combo/perfect/star rules (section 13-14) |
| `meta`       | Currency, moves, save, daily reward, achievements, themes (sections 14, 16-17, 23, 32, 35, 43) |
| `ads`        | AdManager interface + MockAdManager (sections 28-31) |
| `audio`      | AudioManager/HapticManager interfaces (sections 26-27); Android impls do real PCM tone synthesis and `VibrationEffect` |
| `graphics`   | ProceduralArt + CategoryColors - all visuals, no image assets |
| `screens`    | MainMenu, LevelSelect, Themes, Gameplay (sections 19-21) |

## What's implemented (MVP scope, section 76)

- Tap-to-select, tap-to-sort core loop with real Box2D drop/bounce/snap/shake physics
- Procedural level generation across all five difficulty bands in section 7 (levels 1-150+),
  each level validated for solvability (capacity, lock cycles, move budget) before use
- 20 objects across the 5 real categories from section 6, plus Wildcard/Locked/Mystery specials
- Combo scoring (x2-x5), fast-sort bonus, perfect-level bonus, 1-3 star rating
- Coins/gems/stars currencies with a robust single-blob JSON save (section 43)
- 5 cosmetic themes (purchasable, gameplay-neutral per section 24), daily reward with streak,
  6 achievements
- Rewarded-ad hook for "+5 moves" on failure, interstitial cooldown scaffold, Remove Ads stub
- Real Android audio (synthesized tones with combo pitch progression) and haptics (no bundled
  sound files needed)

## What's intentionally stubbed

`MockAdManager` simulates ad flow (a short delay, then success) instead of calling a real ad
network - wiring AdMob/Unity Ads requires your own store account and ad unit IDs. Swap the
implementation passed into `SortEscapeGame` in `AndroidLauncher.kt`; nothing else needs to change
because gameplay code only depends on the `AdManager` interface. Same story for Play Billing /
Remove Ads IAP.

## Building

```bash
./gradlew :core:test              # unit tests: level generator solvability, scoring rules
./gradlew :android:assembleDebug  # debug APK at android/build/outputs/apk/debug/
```

Requires the Android SDK (`ANDROID_HOME`) and a JDK 17. First run downloads Gradle + Maven
dependencies, so it needs network access once.
