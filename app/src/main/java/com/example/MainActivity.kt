package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import com.example.ads.AdPlaybackDialog
import com.example.ads.AdsManager
import com.example.ui.components.AppNavTab
import com.example.ui.screens.completion.LevelCompletionDialog
import com.example.ui.screens.completion.LevelFailedDialog
import com.example.ui.screens.daily.DailyPuzzleScreen
import com.example.ui.screens.gameplay.GameplayScreen
import com.example.ui.screens.onboarding.OnboardingDialog
import com.example.ui.screens.playhub.PlayHubScreen
import com.example.ui.screens.profile.UserProfileDialog
import com.example.ui.screens.ranks.RanksScreen
import com.example.ui.screens.settings.SettingsDialog
import com.example.ui.screens.shop.ShopScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.stats.MindStatisticsScreen
import com.example.ui.theme.MindArrowsTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdsManager.initialize(this)
        setContent {
            MindArrowsApp()
        }
    }
}

@Composable
fun MindArrowsApp(
    viewModel: GameViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val allProgress by viewModel.allProgress.collectAsState()
    val activeGame by viewModel.activeGame.collectAsState()
    val activeAd by AdsManager.activeAd.collectAsState()

    val showSettings by viewModel.showPauseSettingsDialog.collectAsState()
    val showOnboarding by viewModel.showOnboardingDialog.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }

    // User prompt mandate: User game open karne pe sab se pehle apna name and age add kare ga
    LaunchedEffect(userStats.hasCompletedProfile, currentScreen) {
        if (!userStats.hasCompletedProfile && currentScreen != ScreenDestination.SPLASH) {
            showProfileDialog = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    viewModel.soundManager.pauseMusic()
                }
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.soundManager.resumeMusic()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    MindArrowsTheme(darkTheme = userStats.darkThemeEnabled) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Screen Routing
            when (currentScreen) {
                ScreenDestination.SPLASH -> {
                    SplashScreen(
                        onSplashFinished = {
                            viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                        }
                    )
                }
                ScreenDestination.PLAY_HUB -> {
                    PlayHubScreen(
                        userStats = userStats,
                        allProgress = allProgress,
                        activeLevelId = activeGame?.level?.id ?: 1,
                        onLevelSelected = { levelId ->
                            viewModel.loadLevel(levelId)
                            viewModel.navigateTo(ScreenDestination.GAMEPLAY)
                        },
                        onPlayActiveClick = {
                            val nextPlayable = allProgress.firstOrNull { it.isUnlocked && !it.isCompleted }?.levelId ?: 1
                            viewModel.loadLevel(nextPlayable)
                            viewModel.navigateTo(ScreenDestination.GAMEPLAY)
                        },
                        onProfileClick = {
                            viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                        },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                AppNavTab.PLAY -> viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                                AppNavTab.SHOP -> viewModel.navigateTo(ScreenDestination.SHOP)
                                AppNavTab.DAILY -> viewModel.navigateTo(ScreenDestination.DAILY)
                                AppNavTab.RANKS -> viewModel.navigateTo(ScreenDestination.RANKS)
                                AppNavTab.BRAIN -> viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                            }
                        },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onSettingsClick = { viewModel.showPauseSettingsDialog.value = true }
                    )
                }
                ScreenDestination.GAMEPLAY -> {
                    activeGame?.let { game ->
                        BackHandler {
                            viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                        }
                        GameplayScreen(
                            gameState = game,
                            userStats = userStats,
                            onArrowClick = { arrow, isUnblocked ->
                                viewModel.onArrowTapped(arrow, isUnblocked)
                            },
                            onUndoClick = { viewModel.undoMove() },
                            onHintClick = { viewModel.useHint() },
                            onRestartClick = { viewModel.restartLevel() },
                            onBackClick = { viewModel.navigateTo(ScreenDestination.PLAY_HUB) },
                            onPauseClick = { viewModel.showPauseSettingsDialog.value = true },
                            onToggleSound = { viewModel.toggleSound() },
                            onWatchAdForHint = { viewModel.watchAdForFreeHints() }
                        )
                    }
                }
                ScreenDestination.SHOP -> {
                    BackHandler {
                        viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                    }
                    ShopScreen(
                        userStats = userStats,
                        onBuySkin = { skinId, price -> viewModel.buyArrowSkin(skinId, price) },
                        onEquipSkin = { skinId -> viewModel.equipArrowSkin(skinId) },
                        onWatchAdForCoins = { viewModel.watchAdForShopCoins() },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                AppNavTab.PLAY -> viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                                AppNavTab.SHOP -> viewModel.navigateTo(ScreenDestination.SHOP)
                                AppNavTab.DAILY -> viewModel.navigateTo(ScreenDestination.DAILY)
                                AppNavTab.RANKS -> viewModel.navigateTo(ScreenDestination.RANKS)
                                AppNavTab.BRAIN -> viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                            }
                        },
                        onProfileClick = {
                            viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                        },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onSettingsClick = { viewModel.showPauseSettingsDialog.value = true }
                    )
                }
                ScreenDestination.DAILY -> {
                    BackHandler {
                        viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                    }
                    DailyPuzzleScreen(
                        userStats = userStats,
                        onPlayDailyClick = {
                            viewModel.playDailyChallenge()
                            viewModel.navigateTo(ScreenDestination.GAMEPLAY)
                        },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                AppNavTab.PLAY -> viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                                AppNavTab.SHOP -> viewModel.navigateTo(ScreenDestination.SHOP)
                                AppNavTab.DAILY -> viewModel.navigateTo(ScreenDestination.DAILY)
                                AppNavTab.RANKS -> viewModel.navigateTo(ScreenDestination.RANKS)
                                AppNavTab.BRAIN -> viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                            }
                        },
                        onProfileClick = {
                            viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                        },
                        onClaimMission = { id, reward ->
                            viewModel.claimDailyMission(id, reward)
                        },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onSettingsClick = { viewModel.showPauseSettingsDialog.value = true }
                    )
                }
                ScreenDestination.RANKS -> {
                    BackHandler {
                        viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                    }
                    RanksScreen(
                        userStats = userStats,
                        onNavTabSelected = { tab ->
                            when (tab) {
                                AppNavTab.PLAY -> viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                                AppNavTab.SHOP -> viewModel.navigateTo(ScreenDestination.SHOP)
                                AppNavTab.DAILY -> viewModel.navigateTo(ScreenDestination.DAILY)
                                AppNavTab.RANKS -> viewModel.navigateTo(ScreenDestination.RANKS)
                                AppNavTab.BRAIN -> viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                            }
                        },
                        onProfileClick = {
                            showProfileDialog = true
                        },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onSettingsClick = { viewModel.showPauseSettingsDialog.value = true }
                    )
                }
                ScreenDestination.BRAIN_STATS -> {
                    BackHandler {
                        viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                    }
                    MindStatisticsScreen(
                        userStats = userStats,
                        onBackClick = { viewModel.navigateTo(ScreenDestination.PLAY_HUB) },
                        onNavTabSelected = { tab ->
                            when (tab) {
                                AppNavTab.PLAY -> viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                                AppNavTab.SHOP -> viewModel.navigateTo(ScreenDestination.SHOP)
                                AppNavTab.DAILY -> viewModel.navigateTo(ScreenDestination.DAILY)
                                AppNavTab.RANKS -> viewModel.navigateTo(ScreenDestination.RANKS)
                                AppNavTab.BRAIN -> viewModel.navigateTo(ScreenDestination.BRAIN_STATS)
                            }
                        },
                        onProfileClick = {
                            showProfileDialog = true
                        },
                        onEditProfile = {
                            showProfileDialog = true
                        },
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onSettingsClick = { viewModel.showPauseSettingsDialog.value = true }
                    )
                }
            }

            // Level Completion Dialog
            if (activeGame?.showCompleteDialog == true) {
                LevelCompletionDialog(
                    gameState = activeGame!!,
                    onNextLevelClick = { viewModel.playNextLevel() },
                    onReplayClick = { viewModel.restartLevel() },
                    onWatchAdFor2xCoins = { viewModel.watchAdForDoubleCoins() }
                )
            }

            // Level Failed Dialog (Loss due to 0 lives)
            if (activeGame?.showFailedDialog == true) {
                LevelFailedDialog(
                    gameState = activeGame!!,
                    onReviveWithAd = { viewModel.reviveWithAd() },
                    onRestartClick = { viewModel.restartLevel() },
                    onExitClick = {
                        viewModel.dismissFailedDialog()
                        viewModel.navigateTo(ScreenDestination.PLAY_HUB)
                    }
                )
            }

            // User Profile Setup / Edit Dialog
            if (showProfileDialog) {
                UserProfileDialog(
                    initialName = userStats.userName,
                    initialAge = userStats.userAge,
                    isFirstTime = !userStats.hasCompletedProfile,
                    onSaveProfile = { name, age ->
                        viewModel.saveUserProfile(name, age)
                        showProfileDialog = false
                    },
                    onDismiss = { showProfileDialog = false }
                )
            }

            // Settings & Audio Dialog
            if (showSettings) {
                SettingsDialog(
                    userStats = userStats,
                    onDismiss = { viewModel.showPauseSettingsDialog.value = false },
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleMusic = { viewModel.toggleMusic() },
                    onToggleHaptics = { viewModel.toggleHaptics() },
                    onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                    onResetProgress = { viewModel.resetAllProgress() },
                    onWatchAdForReward = { viewModel.watchAdForFreeHints() }
                )
            }

            // Onboarding Dialog
            if (showOnboarding) {
                OnboardingDialog(
                    onDismiss = { viewModel.showOnboardingDialog.value = false }
                )
            }

            // Ad Playback Dialog (Rewarded / Interstitial)
            activeAd?.let { ad ->
                AdPlaybackDialog(
                    adState = ad,
                    onDismiss = { AdsManager.dismissAd() }
                )
            }
        }
    }
}
