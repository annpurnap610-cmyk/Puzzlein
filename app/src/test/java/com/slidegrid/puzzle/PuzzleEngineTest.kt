package com.slidegrid.puzzle

import com.slidegrid.puzzle.logic.PuzzleEngine
import com.slidegrid.puzzle.models.DifficultyMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PuzzleEngineTest {

    @Test
    fun solvedBoardsAreValidAndCompleteForAllDifficulties() {
        for (mode in DifficultyMode.entries) {
            val board = PuzzleEngine.createSolvedBoard(mode.gridSize)
            assertEquals(mode.gridSize * mode.gridSize, board.size)
            assertTrue(PuzzleEngine.isBoardSolved(board))
            assertTrue(PuzzleEngine.isBoardSolvable(board, mode.gridSize))
            assertEquals(mode.tileCount, PuzzleEngine.countPlacedTiles(board))
        }
    }

    @Test
    fun shuffledBoardsAreAlwaysSolvableAndNotAlreadySolved() {
        val random = Random(42)
        for (mode in DifficultyMode.entries) {
            repeat(50) {
                val shuffled = PuzzleEngine.createShuffledBoard(
                    size = mode.gridSize,
                    scrambleMoves = mode.scrambleMoves,
                    random = random
                )
                assertFalse(
                    "Shuffled ${mode.gridSize}x${mode.gridSize} board should not start solved",
                    PuzzleEngine.isBoardSolved(shuffled)
                )
                assertTrue(
                    "Shuffled ${mode.gridSize}x${mode.gridSize} board must be mathematically solvable",
                    PuzzleEngine.isBoardSolvable(shuffled, mode.gridSize)
                )
            }
        }
    }

    @Test
    fun impossibleBoardIsDetectedAsUnsolvable() {
        // Classic 4x4 with tiles 14 and 15 swapped is mathematically unsolvable
        val unsolvable4x4 = listOf(
            1, 2, 3, 4,
            5, 6, 7, 8,
            9, 10, 11, 12,
            13, 15, 14, 0
        )
        assertFalse(PuzzleEngine.isBoardSolvable(unsolvable4x4, 4))

        // Classic 3x3 with tiles 1 and 2 swapped is unsolvable
        val unsolvable3x3 = listOf(
            2, 1, 3,
            4, 5, 6,
            7, 8, 0
        )
        assertFalse(PuzzleEngine.isBoardSolvable(unsolvable3x3, 3))
    }

    @Test
    fun singleAndMultiTileSlidesWorkAndInvalidSlidesReturnNull() {
        val board = listOf(
            1, 2, 3,
            4, 5, 6,
            7, 8, 0
        )

        // Blank is at index 8 (row 2, col 2). Tile at index 0 (row 0, col 0) is diagonal -> invalid
        assertFalse(PuzzleEngine.canSlideTile(board, 0, 3))
        assertNull(PuzzleEngine.slideTileAtIndex(board, 0, 3))

        // Tile at index 6 (row 2, col 0) is in same row as blank -> slides 7 and 8 right
        val multiSlide = PuzzleEngine.slideTileAtIndex(board, 6, 3)
        assertNotNull(multiSlide)
        assertEquals(
            listOf(
                1, 2, 3,
                4, 5, 6,
                0, 7, 8
            ),
            multiSlide!!.nextBoard
        )
        assertEquals(setOf(7, 8), multiSlide.movedTileValues)

        // Slide back to solved state via index 8
        val solvedSlide = PuzzleEngine.slideTileAtIndex(multiSlide.nextBoard, 8, 3)
        assertNotNull(solvedSlide)
        assertTrue(PuzzleEngine.isBoardSolved(solvedSlide!!.nextBoard))
    }
}
