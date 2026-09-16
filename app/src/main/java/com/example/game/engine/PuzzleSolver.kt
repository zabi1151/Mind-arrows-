package com.example.game.engine

import com.example.game.model.Arrow
import com.example.game.model.PuzzleLevel
import java.util.ArrayDeque

object PuzzleSolver {

    /**
     * Solves the level from its initial state, returning the sequence
     * of arrow IDs in the order they should be cleared.
     * Returns null if no solution exists.
     */
    fun solve(level: PuzzleLevel): List<String>? {
        return findSolution(level.arrows, level.gridSize)
    }

    /**
     * Solves from an arbitrary remaining subset of arrows.
     */
    fun findSolution(arrows: List<Arrow>, gridSize: Int): List<String>? {
        if (arrows.isEmpty()) return emptyList()

        // BFS search over puzzle states
        data class SearchNode(val remaining: List<Arrow>, val path: List<String>)

        val queue = ArrayDeque<SearchNode>()
        val visited = HashSet<String>()

        queue.add(SearchNode(arrows, emptyList()))
        visited.add(stateKey(arrows))

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break
            if (current.remaining.isEmpty()) {
                return current.path
            }

            val unblocked = PuzzleEngine.getUnblockedArrows(current.remaining, gridSize)
            for (arrow in unblocked) {
                val nextRemaining = current.remaining.filter { it.id != arrow.id }
                val key = stateKey(nextRemaining)
                if (key !in visited) {
                    visited.add(key)
                    queue.add(SearchNode(nextRemaining, current.path + arrow.id))
                }
            }
        }

        return null // Unsolvable from this state
    }

    /**
     * Generates a hint for the player: returns an Arrow that is currently unblocked
     * and leads toward a guaranteed valid solution.
     */
    fun getOptimalHint(remainingArrows: List<Arrow>, gridSize: Int): Arrow? {
        val solution = findSolution(remainingArrows, gridSize)
        if (!solution.isNullOrEmpty()) {
            val bestArrowId = solution.first()
            return remainingArrows.find { it.id == bestArrowId }
        }
        // Fallback: any currently unblocked arrow
        return PuzzleEngine.getUnblockedArrows(remainingArrows, gridSize).firstOrNull()
    }

    /**
     * Quick verification whether a puzzle state has at least one valid solution.
     */
    fun isSolvable(arrows: List<Arrow>, gridSize: Int): Boolean {
        return findSolution(arrows, gridSize) != null
    }

    private fun stateKey(arrows: List<Arrow>): String {
        return arrows.map { it.id }.sorted().joinToString(",")
    }
}
