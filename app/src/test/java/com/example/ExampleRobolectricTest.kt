package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.engine.PuzzleEngine
import com.example.game.engine.PuzzleSolver
import com.example.game.levels.LevelDatabase
import com.example.game.model.Arrow
import com.example.game.model.Difficulty
import com.example.game.model.Direction
import com.example.game.model.PuzzleLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mind Arrows", appName)
    }

    @Test
    fun `puzzle engine collision and exit detection`() {
        // Arrow 1 at top row pointing UP should be unblocked
        val arrow1 = Arrow("a1", headRow = 0, headCol = 2, direction = Direction.UP, length = 2)
        // Arrow 2 pointing RIGHT blocked by arrow 1
        val arrow2 = Arrow("a2", headRow = 0, headCol = 1, direction = Direction.RIGHT, length = 2)

        val unblocked = PuzzleEngine.getUnblockedArrows(listOf(arrow1, arrow2), gridSize = 5)
        assertTrue(unblocked.any { it.id == "a1" })
    }

    @Test
    fun `iconic level 42 is solvable`() {
        val level42 = LevelDatabase.getLevel(42)
        val solution = PuzzleSolver.solve(level42)
        assertNotNull(solution)
        assertTrue(solution!!.isNotEmpty())
    }

    @Test
    fun `difficulty sectors are properly mapped`() {
        assertEquals(Difficulty.EASY, Difficulty.fromLevel(1))
        assertEquals(Difficulty.EASY, Difficulty.fromLevel(50))
        assertEquals(Difficulty.NORMAL, Difficulty.fromLevel(51))
        assertEquals(Difficulty.HARD, Difficulty.fromLevel(101))
        assertEquals(Difficulty.VERY_HARD, Difficulty.fromLevel(151))
        assertEquals(Difficulty.VERY_HARD, Difficulty.fromLevel(200))
    }
}
