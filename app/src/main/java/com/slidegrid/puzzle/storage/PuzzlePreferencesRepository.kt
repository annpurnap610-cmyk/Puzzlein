package com.slidegrid.puzzle.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.slidegrid.puzzle.models.DifficultyMode
import com.slidegrid.puzzle.models.DifficultyRecord
import com.slidegrid.puzzle.models.PuzzleStats
import com.slidegrid.puzzle.models.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.puzzleDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "slidegrid_preferences"
)

class PuzzlePreferencesRepository(private val context: Context) {

    data class CompletionUpdate(
        val isNewBestMoves: Boolean,
        val isNewBestTime: Boolean
    )

    private object Keys {
        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        val TOTAL_GAMES_COMPLETED = intPreferencesKey("total_games_completed")
        val TOTAL_MOVES = intPreferencesKey("total_moves")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")

        fun bestMovesKey(mode: DifficultyMode) = intPreferencesKey("best_moves_${mode.gridSize}")
        fun bestTimeKey(mode: DifficultyMode) = intPreferencesKey("best_time_${mode.gridSize}")
    }

    val statsFlow: Flow<PuzzleStats> = context.puzzleDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val records = DifficultyMode.entries.associateWith { mode ->
                DifficultyRecord(
                    bestMoves = prefs[Keys.bestMovesKey(mode)],
                    bestTimeSeconds = prefs[Keys.bestTimeKey(mode)]
                )
            }
            PuzzleStats(
                records = records,
                totalGamesPlayed = prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0,
                totalGamesCompleted = prefs[Keys.TOTAL_GAMES_COMPLETED] ?: 0,
                totalMoves = prefs[Keys.TOTAL_MOVES] ?: 0,
                soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
                hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
                themePreference = ThemePreference.fromStorage(prefs[Keys.THEME_PREFERENCE])
            )
        }

    suspend fun recordGameStarted() {
        context.puzzleDataStore.edit { prefs ->
            val current = prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0
            prefs[Keys.TOTAL_GAMES_PLAYED] = current + 1
        }
    }

    suspend fun recordGameCompleted(
        mode: DifficultyMode,
        moves: Int,
        elapsedSeconds: Int
    ): CompletionUpdate {
        var newBestMoves = false
        var newBestTime = false

        context.puzzleDataStore.edit { prefs ->
            val completed = prefs[Keys.TOTAL_GAMES_COMPLETED] ?: 0
            prefs[Keys.TOTAL_GAMES_COMPLETED] = completed + 1

            val totalMoves = prefs[Keys.TOTAL_MOVES] ?: 0
            prefs[Keys.TOTAL_MOVES] = totalMoves + moves

            val movesKey = Keys.bestMovesKey(mode)
            val existingMoves = prefs[movesKey]
            if (existingMoves == null || moves < existingMoves) {
                prefs[movesKey] = moves
                newBestMoves = true
            }

            val timeKey = Keys.bestTimeKey(mode)
            val existingTime = prefs[timeKey]
            if (existingTime == null || elapsedSeconds < existingTime) {
                prefs[timeKey] = elapsedSeconds
                newBestTime = true
            }
        }

        return CompletionUpdate(
            isNewBestMoves = newBestMoves,
            isNewBestTime = newBestTime
        )
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.puzzleDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.puzzleDataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setThemePreference(theme: ThemePreference) {
        context.puzzleDataStore.edit { prefs ->
            prefs[Keys.THEME_PREFERENCE] = theme.storageValue
        }
    }

    suspend fun resetAllRecords() {
        context.puzzleDataStore.edit { prefs ->
            val sound = prefs[Keys.SOUND_ENABLED] ?: true
            val haptics = prefs[Keys.HAPTICS_ENABLED] ?: true
            val theme = prefs[Keys.THEME_PREFERENCE] ?: ThemePreference.DARK.storageValue
            prefs.clear()
            prefs[Keys.SOUND_ENABLED] = sound
            prefs[Keys.HAPTICS_ENABLED] = haptics
            prefs[Keys.THEME_PREFERENCE] = theme
        }
    }
}
