package com.example.game.model

enum class Difficulty(
    val title: String,
    val subtitle: String,
    val minLevel: Int,
    val maxLevel: Int,
    val defaultGridSize: Int
) {
    EASY("Novice", "Easy (1-50)", 1, 50, 6),
    NORMAL("Adept", "Normal (51-100)", 51, 100, 6),
    HARD("Expert", "Hard (101-175)", 101, 175, 7),
    VERY_HARD("Mastermind", "Very Hard (176-240)", 176, 240, 8),
    GRANDMASTER("Grandmaster", "Extreme (241-300)", 241, 300, 9),
    DAILY_EXTREME("Daily Mastermind", "High Difficulty Challenge", 10001, 10001, 9);

    companion object {
        fun fromLevel(level: Int): Difficulty {
            return when {
                level >= 10000 -> DAILY_EXTREME
                level <= 50 -> EASY
                level <= 100 -> NORMAL
                level <= 175 -> HARD
                level <= 240 -> VERY_HARD
                else -> GRANDMASTER
            }
        }
    }
}

data class PuzzleLevel(
    val id: Int,
    val title: String,
    val gridSize: Int,
    val difficulty: Difficulty,
    val arrows: List<Arrow>,
    val targetMoves: Int = arrows.size,
    val targetTimeSeconds: Int = arrows.size * 8
)
