package com.slidegrid.puzzle.models

import com.slidegrid.puzzle.R

enum class DifficultyMode(
    val gridSize: Int,
    val tileCount: Int,
    val scrambleMoves: Int,
    val labelRes: Int,
    val titleRes: Int,
    val tilesRes: Int,
    val descriptionRes: Int
) {
    RELAXED(
        gridSize = 3,
        tileCount = 8,
        scrambleMoves = 120,
        labelRes = R.string.difficulty_relaxed_label,
        titleRes = R.string.difficulty_relaxed_title,
        tilesRes = R.string.difficulty_relaxed_tiles,
        descriptionRes = R.string.difficulty_relaxed_desc
    ),
    CLASSIC(
        gridSize = 4,
        tileCount = 15,
        scrambleMoves = 280,
        labelRes = R.string.difficulty_classic_label,
        titleRes = R.string.difficulty_classic_title,
        tilesRes = R.string.difficulty_classic_tiles,
        descriptionRes = R.string.difficulty_classic_desc
    ),
    MASTER(
        gridSize = 5,
        tileCount = 24,
        scrambleMoves = 520,
        labelRes = R.string.difficulty_master_label,
        titleRes = R.string.difficulty_master_title,
        tilesRes = R.string.difficulty_master_tiles,
        descriptionRes = R.string.difficulty_master_desc
    );

    companion object {
        fun fromGridSize(size: Int): DifficultyMode {
            return entries.firstOrNull { it.gridSize == size } ?: CLASSIC
        }
    }
}
