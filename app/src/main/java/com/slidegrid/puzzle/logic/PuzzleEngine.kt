package com.slidegrid.puzzle.logic

import kotlin.random.Random

object PuzzleEngine {

    data class SlideResult(
        val nextBoard: List<Int>,
        val movedTileValues: Set<Int>,
        val directionX: Int,
        val directionY: Int
    )

    fun createSolvedBoard(size: Int): List<Int> {
        require(size in 3..5) { "Grid size must be between 3 and 5." }
        val total = size * size
        return List(total) { index ->
            if (index == total - 1) 0 else index + 1
        }
    }

    fun isBoardSolved(board: List<Int>): Boolean {
        if (board.isEmpty()) return false
        for (i in 0 until board.size - 1) {
            if (board[i] != i + 1) return false
        }
        return board.last() == 0
    }

    fun countPlacedTiles(board: List<Int>): Int {
        var placed = 0
        for (i in 0 until board.size - 1) {
            if (board[i] == i + 1) placed++
        }
        return placed
    }

    fun getAdjacentIndices(index: Int, size: Int): List<Int> {
        val row = index / size
        val col = index % size
        val neighbors = ArrayList<Int>(4)
        if (row > 0) neighbors.add((row - 1) * size + col)
        if (row < size - 1) neighbors.add((row + 1) * size + col)
        if (col > 0) neighbors.add(row * size + (col - 1))
        if (col < size - 1) neighbors.add(row * size + (col + 1))
        return neighbors
    }

    /**
     * Verifies mathematical solvability using inversion parity and blank row position.
     */
    fun isBoardSolvable(board: List<Int>, size: Int): Boolean {
        if (board.size != size * size) return false
        var inversions = 0
        for (i in board.indices) {
            val a = board[i]
            if (a == 0) continue
            for (j in i + 1 until board.size) {
                val b = board[j]
                if (b != 0 && a > b) {
                    inversions++
                }
            }
        }
        return if (size % 2 == 1) {
            inversions % 2 == 0
        } else {
            val blankIndex = board.indexOf(0)
            val blankRowFromBottom = size - (blankIndex / size)
            if (blankRowFromBottom % 2 == 0) {
                inversions % 2 == 1
            } else {
                inversions % 2 == 0
            }
        }
    }

    /**
     * Generates a guaranteed solvable scrambled board by applying legal blank swaps
     * from the solved state, avoiding immediate reversals and solved endings.
     */
    fun createShuffledBoard(
        size: Int,
        scrambleMoves: Int,
        random: Random = Random.Default
    ): List<Int> {
        val board = createSolvedBoard(size).toMutableList()
        var blankIndex = board.lastIndex
        var previousBlankIndex = -1

        repeat(scrambleMoves) {
            val candidates = getAdjacentIndices(blankIndex, size)
                .filter { it != previousBlankIndex }
            val nextBlankIndex = candidates[random.nextInt(candidates.size)]
            board[blankIndex] = board[nextBlankIndex]
            board[nextBlankIndex] = 0
            previousBlankIndex = blankIndex
            blankIndex = nextBlankIndex
        }

        if (isBoardSolved(board)) {
            val neighbors = getAdjacentIndices(blankIndex, size)
            val swapIndex = neighbors.first()
            board[blankIndex] = board[swapIndex]
            board[swapIndex] = 0
        }

        return board.toList()
    }

    fun canSlideTile(board: List<Int>, tileIndex: Int, size: Int): Boolean {
        if (tileIndex !in board.indices || board[tileIndex] == 0) return false
        val blankIndex = board.indexOf(0)
        if (blankIndex < 0) return false
        val tileRow = tileIndex / size
        val tileCol = tileIndex % size
        val blankRow = blankIndex / size
        val blankCol = blankIndex % size
        return tileRow == blankRow || tileCol == blankCol
    }

    /**
     * Slides one or multiple tiles in the same row or column toward the blank space.
     * Returns null if the tile cannot legally slide.
     */
    fun slideTileAtIndex(board: List<Int>, tileIndex: Int, size: Int): SlideResult? {
        if (!canSlideTile(board, tileIndex, size)) return null

        val blankIndex = board.indexOf(0)
        val tileRow = tileIndex / size
        val tileCol = tileIndex % size
        val blankRow = blankIndex / size
        val blankCol = blankIndex % size

        val next = board.toMutableList()
        val movedValues = HashSet<Int>()
        var dirX = 0
        var dirY = 0

        if (tileRow == blankRow) {
            val step = if (tileCol < blankCol) -1 else 1
            dirX = if (tileCol < blankCol) 1 else -1
            var c = blankCol
            while (c != tileCol) {
                val target = blankRow * size + c
                val source = blankRow * size + (c + step)
                val value = next[source]
                next[target] = value
                movedValues.add(value)
                c += step
            }
            next[tileIndex] = 0
        } else {
            val step = if (tileRow < blankRow) -1 else 1
            dirY = if (tileRow < blankRow) 1 else -1
            var r = blankRow
            while (r != tileRow) {
                val target = r * size + blankCol
                val source = (r + step) * size + blankCol
                val value = next[source]
                next[target] = value
                movedValues.add(value)
                r += step
            }
            next[tileIndex] = 0
        }

        return SlideResult(
            nextBoard = next.toList(),
            movedTileValues = movedValues,
            directionX = dirX,
            directionY = dirY
        )
    }
}
