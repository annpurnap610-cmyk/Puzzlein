package com.slidegrid.puzzle.models

data class DifficultyRecord(
    val bestMoves: Int? = null,
    val bestTimeSeconds: Int? = null
)

data class PuzzleStats(
    val records: Map<DifficultyMode, DifficultyRecord> = emptyMap(),
    val totalGamesPlayed: Int = 0,
    val totalGamesCompleted: Int = 0,
    val totalMoves: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themePreference: ThemePreference = ThemePreference.DARK
) {
    fun completionRatePercent(): Int {
        if (totalGamesPlayed <= 0) return 0
        return ((totalGamesCompleted * 100f) / totalGamesPlayed).toInt().coerceIn(0, 100)
    }
}
