package com.slidegrid.puzzle.models

enum class GameScreen {
    SPLASH,
    MAIN_MENU,
    DIFFICULTY,
    GAME,
    PAUSE,
    VICTORY,
    STATS,
    SETTINGS,
    ABOUT
}

enum class PlayStatus {
    READY,
    PLAYING,
    PAUSED,
    VICTORY
}

enum class ThemePreference(val storageValue: String) {
    DARK("dark"),
    LIGHT("light"),
    SYSTEM("system");

    companion object {
        fun fromStorage(value: String?): ThemePreference {
            return entries.firstOrNull { it.storageValue == value } ?: DARK
        }
    }
}

data class GameUiState(
    val currentScreen: GameScreen = GameScreen.SPLASH,
    val selectedMode: DifficultyMode = DifficultyMode.CLASSIC,
    val activeMode: DifficultyMode = DifficultyMode.CLASSIC,
    val board: List<Int> = emptyList(),
    val moveCount: Int = 0,
    val elapsedSeconds: Int = 0,
    val placedCount: Int = 0,
    val canUndo: Boolean = false,
    val hasStartedMoving: Boolean = false,
    val playStatus: PlayStatus = PlayStatus.READY,
    val isNewBestMoves: Boolean = false,
    val isNewBestTime: Boolean = false,
    val lastMovedTiles: Set<Int> = emptySet(),
    val lastMovePlacedTile: Boolean = false
)
