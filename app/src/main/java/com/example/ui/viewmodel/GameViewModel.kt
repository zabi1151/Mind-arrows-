package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdRewardType
import com.example.ads.AdsManager
import com.example.audio.SoundManager
import com.example.data.model.LevelProgressEntity
import com.example.data.model.UserStatsEntity
import com.example.data.repository.GameRepository
import com.example.game.engine.PuzzleEngine
import com.example.game.engine.PuzzleSolver
import com.example.game.levels.LevelDatabase
import com.example.game.model.Arrow
import com.example.game.model.PuzzleLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenDestination {
    SPLASH,
    PLAY_HUB,
    GAMEPLAY,
    SHOP,
    DAILY,
    RANKS,
    BRAIN_STATS
}

data class ActiveGameState(
    val level: PuzzleLevel,
    val remainingArrows: List<Arrow>,
    val movesCount: Int = 0,
    val elapsedSeconds: Int = 0,
    val lives: Int = 3,
    val maxLives: Int = 3,
    val hintArrowId: String? = null,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val showCompleteDialog: Boolean = false,
    val showFailedDialog: Boolean = false,
    val starsEarned: Int = 3,
    val coinsEarned: Int = 25
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    val soundManager = SoundManager(application)

    val allProgress: StateFlow<List<LevelProgressEntity>> = repository.allProgressFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStatsEntity> = repository.statsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserStatsEntity())

    private val _currentScreen = MutableStateFlow(ScreenDestination.SPLASH)
    val currentScreen: StateFlow<ScreenDestination> = _currentScreen.asStateFlow()

    private val _activeGame = MutableStateFlow<ActiveGameState?>(null)
    val activeGame: StateFlow<ActiveGameState?> = _activeGame.asStateFlow()

    private val moveHistory = mutableListOf<List<Arrow>>()
    private var timerJob: Job? = null

    // Dialog flags
    var showPauseSettingsDialog = MutableStateFlow(false)
    var showOnboardingDialog = MutableStateFlow(false)

    init {
        // Initialize default active level to Level 1 from scratch
        loadLevel(1)

        // Keep audio engine synchronized with user sound and ambient music preferences
        viewModelScope.launch {
            userStats.collect { stats ->
                soundManager.setSoundEnabled(stats.soundEnabled)
                soundManager.setMusicEnabled(stats.musicEnabled)
            }
        }
    }

    fun navigateTo(destination: ScreenDestination) {
        soundManager.playButtonClick(userStats.value.soundEnabled)
        _currentScreen.value = destination
    }

    fun loadLevel(levelId: Int) {
        val level = LevelDatabase.getLevel(levelId)
        moveHistory.clear()
        _activeGame.value = ActiveGameState(
            level = level,
            remainingArrows = level.arrows,
            movesCount = 0,
            elapsedSeconds = 0,
            lives = 3,
            maxLives = 3,
            hintArrowId = null,
            isCompleted = false,
            isFailed = false,
            showCompleteDialog = false,
            showFailedDialog = false
        )
        startTimer()
    }

    fun playDailyChallenge() {
        val dailyLevel = LevelDatabase.getDailyMastermindLevel(userStats.value.dailyStreak + 1)
        moveHistory.clear()
        _activeGame.value = ActiveGameState(
            level = dailyLevel,
            remainingArrows = dailyLevel.arrows,
            movesCount = 0,
            elapsedSeconds = 0,
            lives = 3,
            maxLives = 3,
            hintArrowId = null,
            isCompleted = false,
            isFailed = false,
            showCompleteDialog = false,
            showFailedDialog = false
        )
        startTimer()
        _currentScreen.value = ScreenDestination.GAMEPLAY
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeGame.value ?: break
                if (!current.isCompleted && !showPauseSettingsDialog.value) {
                    _activeGame.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                }
            }
        }
    }

    fun onArrowTapped(arrow: Arrow, isUnblocked: Boolean) {
        val current = _activeGame.value ?: return
        if (current.isCompleted) return

        if (isUnblocked) {
            // Save state for Undo
            moveHistory.add(current.remainingArrows)

            soundManager.playArrowLaunch(userStats.value.soundEnabled)
            soundManager.triggerHaptic(userStats.value.hapticsEnabled, isError = false)

            val updatedArrows = current.remainingArrows.filter { it.id != arrow.id }
            val updatedMoves = current.movesCount + 1

            if (updatedArrows.isEmpty()) {
                // Puzzle Cleared!
                completeLevel(current, updatedMoves)
            } else {
                _activeGame.value = current.copy(
                    remainingArrows = updatedArrows,
                    movesCount = updatedMoves,
                    hintArrowId = if (current.hintArrowId == arrow.id) null else current.hintArrowId
                )
            }
        } else {
            // Blocked collision -> Wrong move! Decrement lives!
            soundManager.playBlocked(userStats.value.soundEnabled)
            soundManager.triggerHaptic(userStats.value.hapticsEnabled, isError = true)

            val newLives = current.lives - 1
            if (newLives <= 0) {
                // All lives lost -> Game Over
                timerJob?.cancel()
                _activeGame.value = current.copy(
                    lives = 0,
                    isCompleted = true,
                    isFailed = true,
                    showFailedDialog = true
                )
            } else {
                _activeGame.value = current.copy(lives = newLives)
            }
        }
    }

    private fun completeLevel(current: ActiveGameState, finalMoves: Int) {
        timerJob?.cancel()
        // Stars depend directly on remaining lives: 3 lives = 3 stars, 2 lives = 2 stars, 1 life = 1 star
        val stars = when (current.lives) {
            3 -> 3
            2 -> 2
            else -> 1
        }
        // Reduced reward coins: 15 base + 5 per star (max 30 coins)
        val coins = 15 + (stars * 5)

        _activeGame.value = current.copy(
            remainingArrows = emptyList(),
            movesCount = finalMoves,
            isCompleted = true,
            isFailed = false,
            showCompleteDialog = true,
            showFailedDialog = false,
            starsEarned = stars,
            coinsEarned = coins
        )

        soundManager.playLevelComplete(userStats.value.soundEnabled)
        soundManager.triggerHaptic(userStats.value.hapticsEnabled, isError = false)

        viewModelScope.launch {
            repository.completeLevel(
                levelId = current.level.id,
                starsEarned = stars,
                timeSeconds = current.elapsedSeconds,
                movesTaken = finalMoves
            )
        }
    }

    fun reviveWithAd() {
        AdsManager.showRewardedAd(AdRewardType.LIVES_REFILL) {
            val current = _activeGame.value ?: return@showRewardedAd
            _activeGame.value = current.copy(
                lives = 3,
                isCompleted = false,
                isFailed = false,
                showFailedDialog = false
            )
            soundManager.playButtonClick(userStats.value.soundEnabled)
            startTimer()
        }
    }

    fun dismissFailedDialog() {
        val current = _activeGame.value ?: return
        _activeGame.value = current.copy(showFailedDialog = false)
    }

    fun undoMove() {
        if (moveHistory.isEmpty()) return
        val current = _activeGame.value ?: return
        val previousArrows = moveHistory.removeAt(moveHistory.size - 1)

        soundManager.playButtonClick(userStats.value.soundEnabled)
        _activeGame.value = current.copy(
            remainingArrows = previousArrows,
            hintArrowId = null
        )
        viewModelScope.launch { repository.recordUndo() }
    }

    fun useHint() {
        val current = _activeGame.value ?: return
        if (current.remainingArrows.isEmpty()) return

        viewModelScope.launch {
            val consumed = repository.consumeHint()
            if (consumed) {
                soundManager.playHint(userStats.value.soundEnabled)
                val hintArrow = PuzzleSolver.getOptimalHint(current.remainingArrows, current.level.gridSize)
                _activeGame.value = current.copy(hintArrowId = hintArrow?.id)
            }
        }
    }

    fun restartLevel() {
        val current = _activeGame.value ?: return
        soundManager.playButtonClick(userStats.value.soundEnabled)
        loadLevel(current.level.id)
    }

    fun playNextLevel() {
        val current = _activeGame.value ?: return
        val nextId = (current.level.id + 1).coerceAtMost(300)
        soundManager.playButtonClick(userStats.value.soundEnabled)

        // Maybe show interstitial ad between levels
        AdsManager.maybeShowInterstitial {
            loadLevel(nextId)
            _currentScreen.value = ScreenDestination.GAMEPLAY
        }
    }

    fun watchAdForDoubleCoins() {
        AdsManager.showRewardedAd(AdRewardType.DOUBLE_COINS) {
            viewModelScope.launch {
                repository.addCoins(40)
                soundManager.playLevelComplete(userStats.value.soundEnabled)
            }
        }
    }

    fun watchAdForFreeHints() {
        AdsManager.showRewardedAd(AdRewardType.HINT_BOOST) {
            viewModelScope.launch {
                repository.addHints(2)
                soundManager.playHint(userStats.value.soundEnabled)
            }
        }
    }

    fun toggleSound() {
        val isAudioActive = userStats.value.soundEnabled || userStats.value.musicEnabled
        val newState = !isAudioActive
        soundManager.setSoundEnabled(newState)
        soundManager.setMusicEnabled(newState)
        viewModelScope.launch {
            repository.updateSettings(sound = newState, music = newState)
        }
    }

    fun toggleMusic() {
        val newState = !userStats.value.musicEnabled
        soundManager.setMusicEnabled(newState)
        viewModelScope.launch {
            repository.updateSettings(music = newState)
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            repository.updateSettings(haptics = !userStats.value.hapticsEnabled)
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            repository.updateSettings(darkTheme = !userStats.value.darkThemeEnabled)
        }
    }

    fun saveUserProfile(name: String, age: Int) {
        viewModelScope.launch {
            repository.saveUserProfile(name, age)
            soundManager.playLevelComplete(userStats.value.soundEnabled)
        }
    }

    fun buyArrowSkin(skinId: String, price: Int) {
        viewModelScope.launch {
            val success = repository.buyArrowSkin(skinId, price)
            if (success) {
                soundManager.playLevelComplete(userStats.value.soundEnabled)
            } else {
                soundManager.playBlocked(userStats.value.soundEnabled)
            }
        }
    }

    fun equipArrowSkin(skinId: String) {
        viewModelScope.launch {
            val success = repository.equipArrowSkin(skinId)
            if (success) {
                soundManager.playButtonClick(userStats.value.soundEnabled)
            }
        }
    }

    fun claimDailyMission(missionId: String, rewardCoins: Int) {
        viewModelScope.launch {
            val success = repository.claimDailyMission(missionId, rewardCoins)
            if (success) {
                soundManager.playCoinReward(userStats.value.soundEnabled)
            }
        }
    }

    fun watchAdForShopCoins() {
        AdsManager.showRewardedAd(AdRewardType.DOUBLE_COINS) {
            viewModelScope.launch {
                repository.addCoins(100)
                soundManager.playCoinReward(userStats.value.soundEnabled)
            }
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            loadLevel(1)
            _currentScreen.value = ScreenDestination.PLAY_HUB
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundManager.release()
    }
}
