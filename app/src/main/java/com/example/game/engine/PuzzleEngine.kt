package com.example.game.engine

import com.example.game.model.Arrow
import com.example.game.model.Cell

object PuzzleEngine {

    /**
     * Checks whether an arrow has an unobstructed exit corridor.
     * An arrow is free/unblocked if none of the cells between its head
     * and the edge of the board are occupied by any other arrow.
     */
    fun isArrowUnblocked(arrow: Arrow, allArrows: List<Arrow>, gridSize: Int): Boolean {
        val path = arrow.forwardPath(gridSize)
        if (path.isEmpty()) {
            // Already at the board boundary edge pointing out!
            return true
        }

        // Build set of all cells occupied by other arrows
        val occupiedByOthers = HashSet<Cell>()
        for (other in allArrows) {
            if (other.id != arrow.id) {
                occupiedByOthers.addAll(other.occupiedCells())
            }
        }

        for (cell in path) {
            if (cell in occupiedByOthers) {
                return false
            }
        }
        return true
    }

    /**
     * Returns all arrows that can legally slide off the board in the current state.
     */
    fun getUnblockedArrows(arrows: List<Arrow>, gridSize: Int): List<Arrow> {
        val occupiedMap = buildOccupiedMap(arrows)
        return arrows.filter { arrow ->
            val path = arrow.forwardPath(gridSize)
            path.none { cell ->
                val occupyingArrowId = occupiedMap[cell]
                occupyingArrowId != null && occupyingArrowId != arrow.id
            }
        }
    }

    /**
     * Finds which arrows directly obstruct this arrow's path.
     */
    fun findBlockingArrows(arrow: Arrow, arrows: List<Arrow>, gridSize: Int): List<Arrow> {
        val path = arrow.forwardPath(gridSize)
        val occupiedMap = buildOccupiedMap(arrows)
        val blockingIds = path.mapNotNull { cell -> occupiedMap[cell] }
            .filter { it != arrow.id }
            .toSet()
        return arrows.filter { it.id in blockingIds }
    }

    private fun buildOccupiedMap(arrows: List<Arrow>): Map<Cell, String> {
        val map = HashMap<Cell, String>()
        for (arrow in arrows) {
            for (cell in arrow.occupiedCells()) {
                map[cell] = arrow.id
            }
        }
        return map
    }
}
