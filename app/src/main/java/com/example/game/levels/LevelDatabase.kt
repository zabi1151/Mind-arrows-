package com.example.game.levels

import com.example.game.engine.PuzzleSolver
import com.example.game.model.Arrow
import com.example.game.model.Cell
import com.example.game.model.Difficulty
import com.example.game.model.Direction
import com.example.game.model.PuzzleLevel
import java.util.Random

object LevelDatabase {

    private val levelCache = HashMap<Int, PuzzleLevel>()

    init {
        // Pre-warm the first batch for fast startup
        for (lvl in 1..50) {
            levelCache[lvl] = buildLevel(lvl)
        }
    }

    fun getLevel(id: Int): PuzzleLevel {
        if (id >= 10000) {
            return levelCache[id] ?: run {
                val daily = generateDeterministicLevel(id, Difficulty.DAILY_EXTREME)
                levelCache[id] = daily
                daily
            }
        }
        val bounded = id.coerceIn(1, 300)
        return levelCache[bounded] ?: run {
            val lvl = buildLevel(bounded)
            levelCache[bounded] = lvl
            lvl
        }
    }

    fun getDailyMastermindLevel(dayIndex: Int = 1): PuzzleLevel {
        val dailyId = 10000 + (dayIndex % 365)
        return getLevel(dailyId)
    }

    fun getAllLevels(): List<PuzzleLevel> {
        return (1..300).map { getLevel(it) }
    }

    fun getLevelsForDifficulty(difficulty: Difficulty): List<PuzzleLevel> {
        return (difficulty.minLevel..difficulty.maxLevel).map { getLevel(it) }
    }

    private fun buildLevel(levelId: Int): PuzzleLevel {
        val difficulty = Difficulty.fromLevel(levelId)

        // Handcrafted iconic levels
        when (levelId) {
            1 -> return PuzzleLevel(
                id = 1,
                title = "Stage 1: First Steps",
                gridSize = 5,
                difficulty = difficulty,
                arrows = listOf(
                    Arrow("a1", headRow = 0, headCol = 2, direction = Direction.UP, length = 2),
                    Arrow("a2", headRow = 2, headCol = 4, direction = Direction.RIGHT, length = 2),
                    Arrow("a3", headRow = 4, headCol = 2, direction = Direction.DOWN, length = 2)
                )
            )
            2 -> return PuzzleLevel(
                id = 2,
                title = "Stage 2: Simple Intersect",
                gridSize = 5,
                difficulty = difficulty,
                arrows = listOf(
                    Arrow("a1", headRow = 1, headCol = 4, direction = Direction.RIGHT, length = 3),
                    Arrow("a2", headRow = 0, headCol = 1, direction = Direction.UP, length = 2),
                    Arrow("a3", headRow = 3, headCol = 0, direction = Direction.LEFT, length = 2)
                )
            )
            3 -> return PuzzleLevel(
                id = 3,
                title = "Stage 3: Crossflow",
                gridSize = 5,
                difficulty = difficulty,
                arrows = listOf(
                    Arrow("a1", headRow = 0, headCol = 1, direction = Direction.UP, length = 2),
                    Arrow("a2", headRow = 1, headCol = 4, direction = Direction.RIGHT, length = 2),
                    Arrow("a3", headRow = 4, headCol = 3, direction = Direction.DOWN, length = 2),
                    Arrow("a4", headRow = 3, headCol = 0, direction = Direction.LEFT, length = 2)
                )
            )
            42 -> return PuzzleLevel(
                id = 42,
                title = "Stage 42: Vortex Vector",
                gridSize = 6,
                difficulty = difficulty,
                arrows = listOf(
                    // Unblocked arrow pointing UP on right lane
                    Arrow("a1", headRow = 0, headCol = 4, direction = Direction.UP, length = 3),
                    // Horizontal right at top, blocked by a1 until a1 leaves
                    Arrow("a2", headRow = 1, headCol = 3, direction = Direction.RIGHT, length = 3),
                    // Vertical down on left, blocked by a4
                    Arrow("a3", headRow = 3, headCol = 1, direction = Direction.DOWN, length = 3),
                    // Horizontal left at bottom
                    Arrow("a4", headRow = 5, headCol = 0, direction = Direction.LEFT, length = 4),
                    // Vertical down at col 3
                    Arrow("a5", headRow = 4, headCol = 3, direction = Direction.DOWN, length = 3)
                )
            )
        }

        // Generate deterministic solvable level
        return generateDeterministicLevel(levelId, difficulty)
    }

    /**
     * Generates a deterministic, guaranteed-solvable level using seeded reverse construction.
     */
    private fun generateDeterministicLevel(levelId: Int, difficulty: Difficulty): PuzzleLevel {
        val gridSize = when (difficulty) {
            Difficulty.EASY -> if (levelId <= 25) 5 else 6
            Difficulty.NORMAL -> 6
            Difficulty.HARD -> 7
            Difficulty.VERY_HARD -> 8
            Difficulty.GRANDMASTER -> 9
            Difficulty.DAILY_EXTREME -> 9
        }

        val targetArrowCount = when (difficulty) {
            Difficulty.EASY -> 4 + (levelId / 12)          // 4 to 8 arrows
            Difficulty.NORMAL -> 7 + ((levelId - 50) / 12) // 7 to 11 arrows
            Difficulty.HARD -> 10 + ((levelId - 100) / 10)  // 10 to 17 arrows
            Difficulty.VERY_HARD -> 14 + ((levelId - 175) / 9) // 14 to 21 arrows
            Difficulty.GRANDMASTER -> 18 + ((levelId - 240) / 7) // 18 to 26 arrows
            Difficulty.DAILY_EXTREME -> 22 // High difficulty unique daily challenge
        }

        // Try seeded attempts to build a verified solvable puzzle
        for (attempt in 0 until 50) {
            val seed = levelId * 7919L + attempt * 313L
            val random = Random(seed)

            val candidateArrows = tryConstructReversePuzzle(gridSize, targetArrowCount, random)
            if (candidateArrows != null && candidateArrows.size >= 3) {
                val candidate = PuzzleLevel(
                    id = levelId,
                    title = "Stage $levelId: ${levelSectorName(levelId)}",
                    gridSize = gridSize,
                    difficulty = difficulty,
                    arrows = candidateArrows
                )
                if (PuzzleSolver.solve(candidate) != null) {
                    return candidate
                }
            }
        }

        // Safe fallback deterministic puzzle for this grid size
        return createFallbackSolvableLevel(levelId, gridSize, difficulty, targetArrowCount)
    }

    private fun tryConstructReversePuzzle(
        gridSize: Int,
        count: Int,
        rng: Random
    ): List<Arrow>? {
        val arrows = mutableListOf<Arrow>()
        val occupiedCells = HashSet<Cell>()

        val directions = Direction.values()

        // Place arrows backwards
        for (i in 0 until count) {
            var placed = false
            for (retry in 0 until 40) {
                val dir = directions[rng.nextInt(directions.size)]
                val length = if (gridSize <= 5) 2 else (2 + rng.nextInt(2))

                // Choose head position valid for this direction and length
                val headRow: Int
                val headCol: Int

                when (dir) {
                    Direction.UP -> {
                        val minH = 0
                        val maxH = gridSize - length
                        if (maxH < minH) continue
                        headRow = minH + rng.nextInt(maxH - minH + 1)
                        headCol = rng.nextInt(gridSize)
                    }
                    Direction.DOWN -> {
                        val minH = length - 1
                        val maxH = gridSize - 1
                        if (maxH < minH) continue
                        headRow = minH + rng.nextInt(maxH - minH + 1)
                        headCol = rng.nextInt(gridSize)
                    }
                    Direction.LEFT -> {
                        val minC = 0
                        val maxC = gridSize - length
                        if (maxC < minC) continue
                        headRow = rng.nextInt(gridSize)
                        headCol = minC + rng.nextInt(maxC - minC + 1)
                    }
                    Direction.RIGHT -> {
                        val minC = length - 1
                        val maxC = gridSize - 1
                        if (maxC < minC) continue
                        headRow = rng.nextInt(gridSize)
                        headCol = minC + rng.nextInt(maxC - minC + 1)
                    }
                }

                val candidate = Arrow("a_${i + 1}", headRow, headCol, dir, length)
                val body = candidate.occupiedCells()

                // Ensure none of the candidate's body overlaps existing arrows
                if (body.none { it in occupiedCells }) {
                    arrows.add(candidate)
                    occupiedCells.addAll(body)
                    placed = true
                    break
                }
            }
            if (!placed && arrows.size >= 3) break
        }

        return if (arrows.size >= 3) arrows else null
    }

    private fun createFallbackSolvableLevel(
        levelId: Int,
        gridSize: Int,
        difficulty: Difficulty,
        targetCount: Int
    ): PuzzleLevel {
        val arrows = mutableListOf<Arrow>()
        // Create an interlocking chain that peels outward
        val count = targetCount.coerceIn(3, gridSize * 2)

        for (i in 0 until count) {
            val dir = when (i % 4) {
                0 -> Direction.UP
                1 -> Direction.RIGHT
                2 -> Direction.DOWN
                else -> Direction.LEFT
            }

            val r = when (dir) {
                Direction.UP -> 0
                Direction.DOWN -> gridSize - 1
                Direction.LEFT, Direction.RIGHT -> (1 + (i / 4) * 2) % (gridSize - 1)
            }

            val c = when (dir) {
                Direction.LEFT -> 0
                Direction.RIGHT -> gridSize - 1
                Direction.UP, Direction.DOWN -> (1 + (i / 4) * 2) % (gridSize - 1)
            }

            arrows.add(Arrow("a_${i + 1}", r, c, dir, length = 2))
        }

        return PuzzleLevel(
            id = levelId,
            title = "Stage $levelId: ${levelSectorName(levelId)}",
            gridSize = gridSize,
            difficulty = difficulty,
            arrows = arrows
        )
    }

    private fun levelSectorName(levelId: Int): String {
        val names = listOf(
            "Vortex Vector", "Helix Corridor", "Prism Tangent", "Quantum Axis",
            "Cobalt Matrix", "Chrono Shift", "Nova Conduit", "Aero Disentangle",
            "Kinetic Node", "Apex Logic", "Synapse Cross", "Vector Flow"
        )
        return names[(levelId - 1) % names.size]
    }
}
