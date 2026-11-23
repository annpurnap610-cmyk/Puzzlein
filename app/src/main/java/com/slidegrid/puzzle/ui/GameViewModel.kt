package com.slidegrid.puzzle.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.slidegrid.puzzle.logic.PuzzleEngine
import com.slidegrid.puzzle.logic.SoundEngine
import com.slidegrid.puzzle.models.DifficultyMode
import com.slidegrid.puzzle.models.GameScreen
import com.slidegrid.puzzle.models.GameUiState
import com.slidegrid.puzzle.models.PlayStatus
import com.slidegrid.puzzle.models.PuzzleStats
import com.slidegrid.puzzle.models.ThemePreference
import com.slidegrid.puzzle.storage.PuzzlePreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PuzzlePreferencesRepository(application.applicationContext)
    private val soundEngine = SoundEngine(application.applicationContext)

    val stats: StateFlow<PuzzleStats> = repository.statsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = PuzzleStats()
    )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val moveHistory = ArrayDeque<List<Int>>()
    private var timerJob: Job? = null
    private var isTileAnimating: Boolean = false
    private val navigationHistory = ArrayDeque<GameScreen>()

    fun finishSplash() {
        if (_uiState.value.currentScreen == GameScreen.SPLASH) {
            _uiState.update { it.copy(currentScreen = GameScreen.MAIN_MENU) }
        }
    }

    fun navigateTo(screen: GameScreen, recordBackStack: Boolean = true) {
        val current = _uiState.value.currentScreen
        if (current == screen) return
        if (recordBackStack && current != GameScreen.SPLASH) {
            navigationHistory.addLast(current)
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectDifficulty(mode: DifficultyMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun startNewGame(mode: DifficultyMode = _uiState.value.selectedMode) {
        stopTimer()
        moveHistory.clear()
        isTileAnimating = false

        val initialBoard = PuzzleEngine.createShuffledBoard(
            size = mode.gridSize,
            scrambleMoves = mode.scrambleMoves
        )
        val initialPlaced = PuzzleEngine.countPlacedTiles(initialBoard)

        viewModelScope.launch {
            repository.recordGameStarted()
        }

        _uiState.update {
            it.copy(
                currentScreen = GameScreen.GAME,
                selectedMode = mode,
                activeMode = mode,
                board = initialBoard,
                moveCount = 0,
                elapsedSeconds = 0,
                placedCount = initialPlaced,
                canUndo = false,
                hasStartedMoving = false,
                playStatus = PlayStatus.PLAYING,
                isNewBestMoves = false,
                isNewBestTime = false,
                lastMovedTiles = emptySet(),
                lastMovePlacedTile = false
            )
        }
    }

    fun onTileAnimationChanged(animating: Boolean) {
        isTileAnimating = animating
    }

    fun onTileTapped(tileIndex: Int) {
        val state = _uiState.value
        if (state.currentScreen != GameScreen.GAME ||
            state.playStatus != PlayStatus.PLAYING ||
            isTileAnimating
        ) {
            return
        }

        val slideResult = PuzzleEngine.slideTileAtIndex(
            board = state.board,
            tileIndex = tileIndex,
            size = state.activeMode.gridSize
        ) ?: return

        val prevPlaced = state.placedCount
        val nextPlaced = PuzzleEngine.countPlacedTiles(slideResult.nextBoard)
        val nextMoveCount = state.moveCount + 1
        val wasFirstMove = !state.hasStartedMoving

        moveHistory.addLast(state.board)

        if (PuzzleEngine.isBoardSolved(slideResult.nextBoard)) {
            stopTimer()
            soundEngine.playVictory(stats.value.soundEnabled)

            _uiState.update {
                it.copy(
                    board = slideResult.nextBoard,
                    moveCount = nextMoveCount,
                    placedCount = nextPlaced,
                    canUndo = false,
                    hasStartedMoving = true,
                    playStatus = PlayStatus.VICTORY,
                    currentScreen = GameScreen.VICTORY,
                    lastMovedTiles = slideResult.movedTileValues,
                    lastMovePlacedTile = true
                )
            }

            viewModelScope.launch {
                val update = repository.recordGameCompleted(
                    mode = state.activeMode,
                    moves = nextMoveCount,
                    elapsedSeconds = state.elapsedSeconds
                )
                _uiState.update {
                    it.copy(
                        isNewBestMoves = update.isNewBestMoves,
                        isNewBestTime = update.isNewBestTime
                    )
                }
            }
        } else {
            val placedIncrease = nextPlaced > prevPlaced
            soundEngine.playSlide(stats.value.soundEnabled, placedIncrease)

            _uiState.update {
                it.copy(
                    board = slideResult.nextBoard,
                    moveCount = nextMoveCount,
                    placedCount = nextPlaced,
                    canUndo = moveHistory.isNotEmpty(),
                    hasStartedMoving = true,
                    lastMovedTiles = slideResult.movedTileValues,
                    lastMovePlacedTile = placedIncrease
                )
            }

            if (wasFirstMove) {
                startTimer()
            }
        }
    }

    fun undoLastMove() {
        val state = _uiState.value
        if (state.playStatus != PlayStatus.PLAYING || isTileAnimating || moveHistory.isEmpty()) {
            return
        }

        val previousBoard = moveHistory.removeLast()
        val nextPlaced = PuzzleEngine.countPlacedTiles(previousBoard)
        soundEngine.playSlide(stats.value.soundEnabled, false)

        _uiState.update {
            it.copy(
                board = previousBoard,
                moveCount = (it.moveCount - 1).coerceAtLeast(0),
                placedCount = nextPlaced,
                canUndo = moveHistory.isNotEmpty(),
                lastMovedTiles = emptySet(),
                lastMovePlacedTile = false
            )
        }
    }

    fun pauseGame() {
        val state = _uiState.value
        if (state.currentScreen == GameScreen.GAME && state.playStatus == PlayStatus.PLAYING) {
            stopTimer()
            _uiState.update {
                it.copy(
                    playStatus = PlayStatus.PAUSED,
                    currentScreen = GameScreen.PAUSE
                )
            }
        }
    }

    fun resumeGame() {
        val state = _uiState.value
        if (state.playStatus == PlayStatus.PAUSED) {
            _uiState.update {
                it.copy(
                    playStatus = PlayStatus.PLAYING,
                    currentScreen = GameScreen.GAME
                )
            }
            if (state.hasStartedMoving) {
                startTimer()
            }
        }
    }

    fun onAppBackgrounded() {
        if (_uiState.value.currentScreen == GameScreen.GAME &&
            _uiState.value.playStatus == PlayStatus.PLAYING
        ) {
            pauseGame()
        }
    }

    fun handleBackNavigation(): Boolean {
        return when (_uiState.value.currentScreen) {
            GameScreen.SPLASH -> true
            GameScreen.MAIN_MENU -> false
            GameScreen.GAME -> {
                pauseGame()
                true
            }
            GameScreen.PAUSE -> {
                resumeGame()
                true
            }
            GameScreen.VICTORY -> {
                navigationHistory.clear()
                _uiState.update { it.copy(currentScreen = GameScreen.DIFFICULTY) }
                true
            }
            GameScreen.DIFFICULTY,
            GameScreen.STATS,
            GameScreen.SETTINGS,
            GameScreen.ABOUT -> {
                val previous = if (navigationHistory.isNotEmpty()) {
                    navigationHistory.removeLast()
                } else {
                    GameScreen.MAIN_MENU
                }
                _uiState.update { it.copy(currentScreen = previous) }
                true
            }
        }
    }

    fun returnToMainMenu() {
        stopTimer()
        navigationHistory.clear()
        _uiState.update {
            it.copy(
                currentScreen = GameScreen.MAIN_MENU,
                playStatus = PlayStatus.READY
            )
        }
    }

    fun openDifficultyFromGame() {
        stopTimer()
        navigationHistory.clear()
        _uiState.update {
            it.copy(
                currentScreen = GameScreen.DIFFICULTY,
                playStatus = PlayStatus.READY
            )
        }
    }

    fun toggleSound() {
        val next = !stats.value.soundEnabled
        viewModelScope.launch {
            repository.setSoundEnabled(next)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun setThemePreference(theme: ThemePreference) {
        viewModelScope.launch {
            repository.setThemePreference(theme)
        }
    }

    fun resetAllRecords() {
        viewModelScope.launch {
            repository.resetAllRecords()
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value
                if (state.currentScreen == GameScreen.GAME &&
                    state.playStatus == PlayStatus.PLAYING &&
                    state.hasStartedMoving
                ) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                } else {
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        stopTimer()
        soundEngine.release()
        super.onCleared()
    }
}
