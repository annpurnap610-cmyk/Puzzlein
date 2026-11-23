# SlideGrid — Native Android Studio Project Guide

## 1. Environment Note
The SlideGrid Kotlin + XML + Gradle Kotlin DSL project files have been generated in this workspace (`app/`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`).

Because this web-container workspace does not include a JDK, Kotlin compiler, Gradle binary, or Android SDK, **Gradle sync, Android compilation, unit/UI test execution, and APK generation were not executed here** and must be run locally in Android Studio.

---

## 2. Opening & Building in Android Studio

1. **Open Project**:
   - Launch **Android Studio** (Ladybug / 2024.2+ recommended).
   - Choose **File → Open** and select this project root directory containing `settings.gradle.kts`.
2. **Gradle Sync**:
   - Android Studio reads `gradle/wrapper/gradle-wrapper.properties` (`gradle-8.9-bin.zip`) and provisions the Gradle wrapper automatically during sync.
   - Ensure **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** is set to **JDK 17**.
3. **Run Unit Tests**:
   - In Android Studio, right-click `app/src/test/java/com/slidegrid/puzzle/PuzzleEngineTest.kt` and choose **Run 'PuzzleEngineTest'**, or run via terminal after wrapper setup:
     ```bash
     ./gradlew testDebugUnitTest
     ```
4. **Build & Run Debug App**:
   - Select an Android Emulator or physical Android device (API 24+ / Android 7.0 or higher).
   - Click **Run 'app'** (`Shift+F10`), or run:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 3. Generating a Signed Release APK

### Option A — Android Studio Wizard (Recommended)
1. Select **Build → Generate Signed App Bundle / APK...**
2. Choose **APK** and click **Next**.
3. Click **Create new...** (or choose an existing `.jks` keystore), fill in keystore path, passwords, key alias, and validity.
4. Select build variant **release** (uses R8 minification & resource shrinking configured in `app/build.gradle.kts`).
5. Click **Create**; the signed APK is output to `app/release/app-release.apk`.

### Option B — Command Line Signing Configuration
1. Generate an upload/release keystore (if you do not have one):
   ```bash
   keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias slidegrid
   ```
2. Configure `signingConfigs` in `app/build.gradle.kts` (reading credentials from local environment variables or `keystore.properties`, never hardcoded) and run:
   ```bash
   ./gradlew assembleRelease
   ```

---

## 4. Testing Checklist (Execute Locally Before Release)

- [ ] **Gradle Sync & Build**: `./gradlew assembleDebug` and `./gradlew assembleRelease` succeed cleanly.
- [ ] **Unit Tests**: `PuzzleEngineTest` passes (solvability parity, multi-tile slides, victory check across 3×3, 4×4, 5×5).
- [ ] **Splash & Main Menu**: App launches via AndroidX SplashScreen into `SplashFragment` and transitions smoothly to `MainMenuFragment`.
- [ ] **Difficulty Selection**: Verify 3×3 Relaxed (8 tiles), 4×4 Classic (15 tiles), and 5×5 Master (24 tiles) cards show accurate mini-grids, descriptions, and personal bests.
- [ ] **Gameplay Mechanics**:
  - [ ] Verify single-tile and multi-tile row/column slides work smoothly with 150ms animation.
  - [ ] Verify invalid (diagonal/non-aligned) tile taps do nothing and do not increment moves.
  - [ ] Verify rapid taps during tile animation do not cause double-moves or visual glitches.
  - [ ] Verify timer starts only after the first valid move.
  - [ ] Verify **Undo** restores board state and decrements move count.
- [ ] **Lifecycle, Pause & Back Handling**:
  - [ ] Pressing **Pause** or system Back during gameplay hides tiles on `PauseFragment` and freezes the timer.
  - [ ] Minimizing app (`Home` / `onStop`) automatically pauses an active game and stops the timer.
  - [ ] Rotating screen keeps current board, moves, and timer intact (`GameViewModel` + `!isChangingConfigurations`).
- [ ] **Victory & Persistence**:
  - [ ] Solving each grid size stops the timer, plays victory sound, updates DataStore records, and shows `VictoryFragment`.
  - [ ] Force-closing and reopening app (and device reboot) retains best moves, best times, lifetime counts, sound/haptics toggles, and theme choice.
- [ ] **All Screens & Responsive Sizing**: Verify `StatsFragment`, `SettingsFragment` (Dark/Light/System theme), `AboutFragment`, and board scaling across compact phones, large phones, and tablets.
