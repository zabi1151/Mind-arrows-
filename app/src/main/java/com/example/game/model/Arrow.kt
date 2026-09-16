package com.example.game.model

data class Cell(val row: Int, val col: Int)

data class Arrow(
    val id: String,
    val headRow: Int,
    val headCol: Int,
    val direction: Direction,
    val length: Int = 2
) {
    /**
     * Cells occupied by the body of this arrow on the grid.
     * The head is at (headRow, headCol), and the body stretches
     * in the direction OPPOSITE to the pointing direction.
     */
    fun occupiedCells(): List<Cell> {
        val cells = ArrayList<Cell>(length)
        for (i in 0 until length) {
            val r = headRow - direction.dr * i
            val c = headCol - direction.dc * i
            cells.add(Cell(r, c))
        }
        return cells
    }

    /**
     * Cells that this arrow must traverse from its head forward
     * to the edge of the board in order to escape.
     */
    fun forwardPath(gridSize: Int): List<Cell> {
        val path = mutableListOf<Cell>()
        var currRow = headRow + direction.dr
        var currCol = headCol + direction.dc

        while (currRow in 0 until gridSize && currCol in 0 until gridSize) {
            path.add(Cell(currRow, currCol))
            currRow += direction.dr
            currCol += direction.dc
        }
        return path
    }
}
