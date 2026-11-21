package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ludoblitz.model.AIDifficulty
import com.example.ludoblitz.model.ClassicMatchConfig
import com.example.ludoblitz.model.ClassicPlayerCount
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.ui.components.AchievementUnlockedBanner
import com.example.ludoblitz.ui.components.AnimatedArcadeBackground
import com.example.ludoblitz.ui.components.BottomNavBar
import com.example.ludoblitz.ui.components.CurrencyFlyOverlay
import com.example.ludoblitz.ui.components.LevelUpCelebrationDialog
import com.example.ludoblitz.ui.components.TopHeader
import com.example.ludoblitz.ui.screens.BattleResultScreen
import com.example.ludoblitz.ui.screens.BattleScreen
import com.example.ludoblitz.ui.screens.HomeScreen
import com.example.ludoblitz.ui.screens.LeaderboardScreen
import com.example.ludoblitz.ui.screens.MatchmakingScreen
import com.example.ludoblitz.ui.screens.ProfileScreen
import com.example.ludoblitz.ui.screens.QuestsScreen
import com.example.ludoblitz.ui.screens.SeasonPassScreen
import com.example.ludoblitz.ui.screens.SettingsScreen
import com.example.ludoblitz.ui.screens.ShopScreen
import com.example.ludoblitz.viewmodel.AppScreen
import com.example.ludoblitz.viewmodel.BattleViewModel
import com.example.ludoblitz.viewmodel.MainViewModel
import com.example.ui.theme.LudoBlitzTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val battleViewModel: BattleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LudoBlitzTheme {
                LudoBlitzApp(
                    mainViewModel = mainViewModel,
                    battleViewModel = battleViewModel
                )
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mainViewModel.onAppPause()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.onAppResume()
    }
}

@Composable
fun LudoBlitzApp(
    mainViewModel: MainViewModel,
    battleViewModel: BattleViewModel
) {
    val context = LocalContext.current
    val currentScreen by mainViewModel.currentScreen.collectAsState()
    val profile by mainViewModel.profileFlow.collectAsState()
    val savedGameState by mainViewModel.savedGameFlow.collectAsState()
    val dailyMissions by mainViewModel.dailyMissionsFlow.collectAsState()
    val weeklyMissions by mainViewModel.weeklyMissionsFlow.collectAsState()
    val achievements by mainViewModel.achievementsFlow.collectAsState()
    val leaderboardEntries by mainViewModel.leaderboardEntries.collectAsState()
    val toastMsg by mainViewModel.toastMessage.collectAsState()
    val currencyBurst by mainViewModel.currencyBurst.collectAsState()
    val levelUpEvent by mainViewModel.levelUpEvent.collectAsState()
    val achievementBanner by mainViewModel.achievementBanner.collectAsState()
    val freeSpinsLeft by mainViewModel.freeSpinsRemaining.collectAsState()
    val lastMatchEndResult by battleViewModel.lastMatchEndResult.collectAsState()

    var activeBattleMode by remember { mutableStateOf(GameMode.QUICK_BATTLE) }
    var lastClassicConfig by remember { mutableStateOf<ClassicMatchConfig?>(null) }

    // Sync sound/music/volume/vibration preferences to battle viewmodel
    LaunchedEffect(profile) {
        battleViewModel.soundEnabled = profile.soundEnabled
        battleViewModel.vibrationEnabled = profile.vibrationEnabled
        battleViewModel.soundManager.updatePreferences(
            soundEnabled = profile.soundEnabled,
            musicEnabled = profile.musicEnabled,
            vibrationEnabled = profile.vibrationEnabled,
            musicVolume = profile.musicVolume,
            sfxVolume = profile.sfxVolume
        )
    }

    // Forward post-match progression (level-ups & newly unlocked achievements) to MainViewModel
    LaunchedEffect(lastMatchEndResult) {
        lastMatchEndResult?.let { result ->
            mainViewModel.handleMatchEndResult(result)
            battleViewModel.clearLastMatchEndResult()
        }
    }

    // Handle Toast notifications
    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            mainViewModel.clearToast()
        }
    }

    // Level-Up Celebration Dialog
    levelUpEvent?.let { event ->
        LevelUpCelebrationDialog(
            event = event,
            onDismiss = { mainViewModel.clearLevelUpEvent() }
        )
    }

    // Handle hardware Back from secondary tabs back to Home with home chime
    if (currentScreen == AppScreen.SHOP ||
        currentScreen == AppScreen.SEASON_PASS ||
        currentScreen == AppScreen.QUESTS ||
        currentScreen == AppScreen.LEADERBOARD ||
        currentScreen == AppScreen.PROFILE ||
        currentScreen == AppScreen.SETTINGS
    ) {
        BackHandler {
            mainViewModel.navigateTo(AppScreen.HOME)
        }
    }

    val showHeaderAndNav = currentScreen != AppScreen.BATTLE &&
            currentScreen != AppScreen.MATCHMAKING &&
            currentScreen != AppScreen.BATTLE_RESULT

    Box(modifier = Modifier.fillMaxSize()) {
        // Rich Animated Cosmic Arcade Background behind every screen
        AnimatedArcadeBackground()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                if (showHeaderAndNav) {
                    TopHeader(
                        profile = profile,
                        onProfileClick = { mainViewModel.navigateTo(AppScreen.PROFILE) },
                        onSettingsClick = { mainViewModel.navigateTo(AppScreen.SETTINGS) }
                    )
                }
            },
            bottomBar = {
                if (showHeaderAndNav) {
                    BottomNavBar(
                        currentScreen = currentScreen,
                        onTabSelected = { selectedScreen ->
                            mainViewModel.navigateTo(selectedScreen)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(220),
                    label = "screenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                profile = profile,
                                savedGameState = savedGameState,
                                freeSpinsLeft = freeSpinsLeft,
                                onResumeGame = { savedState ->
                                    mainViewModel.playClickSound()
                                    if (savedState.isClassic) {
                                        lastClassicConfig = ClassicMatchConfig(
                                            playerCount = if (savedState.activePlayers.size >= 4) ClassicPlayerCount.FOUR_PLAYERS else ClassicPlayerCount.TWO_PLAYERS,
                                            isPassAndPlay = savedState.isPassAndPlay,
                                            playerTypes = savedState.playerTypes
                                        )
                                        activeBattleMode = GameMode.CLASSIC
                                    } else {
                                        lastClassicConfig = null
                                        activeBattleMode = savedState.mode
                                    }
                                    battleViewModel.resumeSavedGame(savedState)
                                    mainViewModel.navigateTo(AppScreen.BATTLE)
                                },
                                onStartMatch = { mode ->
                                    mainViewModel.playClickSound()
                                    lastClassicConfig = null
                                    activeBattleMode = mode
                                    mainViewModel.navigateTo(AppScreen.MATCHMAKING)
                                },
                                onStartClassicMatch = { config ->
                                    mainViewModel.playClickSound()
                                    lastClassicConfig = config
                                    activeBattleMode = GameMode.CLASSIC
                                    battleViewModel.startClassicGame(config)
                                    mainViewModel.navigateTo(AppScreen.BATTLE)
                                },
                                onOpenChest = { coins, gems ->
                                    mainViewModel.playChestOpenReward(coins, gems)
                                },
                                onWheelTick = {
                                    mainViewModel.playWheelTick()
                                },
                                onLuckyWheelPrizeWon = { coins, gems, cost, prizeName ->
                                    mainViewModel.claimLuckyWheelReward(coins, gems, cost, prizeName)
                                },
                                onOpenQuests = { mainViewModel.navigateTo(AppScreen.QUESTS) },
                                onCompleteTutorial = {
                                    mainViewModel.completeTutorial()
                                }
                            )
                        }
                        AppScreen.MATCHMAKING -> {
                            MatchmakingScreen(
                                profile = profile,
                                mode = activeBattleMode,
                                onOpponentFoundSound = {
                                    mainViewModel.audioService.playMatchFound(profile.soundEnabled, profile.vibrationEnabled)
                                },
                                onCountdownBeepSound = { num ->
                                    mainViewModel.audioService.playCountdownBeep(num, profile.soundEnabled, profile.vibrationEnabled)
                                },
                                onMatchFound = {
                                    battleViewModel.startBattle(
                                        mode = activeBattleMode,
                                        difficulty = when (activeBattleMode) {
                                            GameMode.PRACTICE -> AIDifficulty.EASY
                                            GameMode.RANKED -> AIDifficulty.HARD
                                            else -> AIDifficulty.MEDIUM
                                        }
                                    )
                                    mainViewModel.navigateTo(AppScreen.BATTLE)
                                }
                            )
                        }
                        AppScreen.BATTLE -> {
                            BattleScreen(
                                battleViewModel = battleViewModel,
                                profile = profile,
                                onMatchCompleted = {
                                    mainViewModel.navigateTo(AppScreen.BATTLE_RESULT)
                                },
                                onExitToHome = {
                                    mainViewModel.navigateTo(AppScreen.HOME)
                                },
                                onToggleAudioQuick = {
                                    mainViewModel.toggleQuickBattleAudio()
                                }
                            )
                        }
                        AppScreen.BATTLE_RESULT -> {
                            BattleResultScreen(
                                battleViewModel = battleViewModel,
                                profile = profile,
                                onRematch = {
                                    mainViewModel.playClickSound()
                                    val classicCfg = lastClassicConfig
                                    if (activeBattleMode == GameMode.CLASSIC && classicCfg != null) {
                                        battleViewModel.startClassicGame(classicCfg)
                                        mainViewModel.navigateTo(AppScreen.BATTLE)
                                    } else {
                                        mainViewModel.navigateTo(AppScreen.MATCHMAKING)
                                    }
                                },
                                onHome = { coinsEarned, gemsEarned ->
                                    mainViewModel.returnHomeWithRewardBurst(coinsEarned, gemsEarned)
                                }
                            )
                        }
                        AppScreen.SHOP -> {
                            ShopScreen(
                                profile = profile,
                                onBuyCosmetic = { id, coins, gems ->
                                    mainViewModel.buyCosmetic(id, coins, gems)
                                },
                                onEquipCosmetic = { id, type ->
                                    mainViewModel.equipCosmetic(id, type)
                                }
                            )
                        }
                        AppScreen.SEASON_PASS -> {
                            SeasonPassScreen(
                                profile = profile,
                                onUnlockVipPass = {
                                    mainViewModel.unlockVipPassWithGems(100)
                                }
                            )
                        }
                        AppScreen.QUESTS -> {
                            QuestsScreen(
                                profile = profile,
                                dailyMissions = dailyMissions,
                                weeklyMissions = weeklyMissions,
                                achievements = achievements,
                                onClaimDailyReward = { day, coins, gems, xp ->
                                    mainViewModel.claimDailyReward(day, coins, gems, xp)
                                },
                                onClaimQuest = { quest, isDaily ->
                                    mainViewModel.claimQuest(quest, isDaily)
                                }
                            )
                        }
                        AppScreen.LEADERBOARD -> {
                            LeaderboardScreen(
                                profile = profile,
                                entries = leaderboardEntries
                            )
                        }
                        AppScreen.PROFILE -> {
                            ProfileScreen(
                                profile = profile,
                                onClaimMilestone = { level ->
                                    mainViewModel.claimLevelMilestone(level)
                                }
                            )
                        }
                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                profile = profile,
                                onToggleSound = { mainViewModel.toggleSound(it) },
                                onToggleMusic = { mainViewModel.toggleMusic(it) },
                                onToggleVibration = { mainViewModel.toggleVibration(it) },
                                onSelectLanguage = { mainViewModel.setLanguage(it) },
                                onMusicVolumeChange = { mainViewModel.setMusicVolume(it) },
                                onSfxVolumeChange = { mainViewModel.setSfxVolume(it) },
                                onToggleMasterMute = { mainViewModel.setMasterMute(it) }
                            )
                        }
                    }
                }
            }
        }

        // Global Moving Duck Coins & Gems Flying Particle Burst Overlay
        CurrencyFlyOverlay(
            burstEvent = currencyBurst,
            onBurstCompleted = { mainViewModel.clearCurrencyBurst() }
        )

        // Global Animated Achievement Unlock Popup Banner
        AchievementUnlockedBanner(
            achievement = achievementBanner,
            onDismiss = { mainViewModel.clearAchievementBanner() },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
