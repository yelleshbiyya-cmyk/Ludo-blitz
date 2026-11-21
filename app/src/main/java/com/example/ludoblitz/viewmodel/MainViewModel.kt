package com.example.ludoblitz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ludoblitz.data.AppDatabase
import com.example.ludoblitz.data.GameRepository
import com.example.ludoblitz.data.MatchEndResult
import com.example.ludoblitz.data.XpAwardResult
import com.example.ludoblitz.model.CosmeticType
import com.example.ludoblitz.model.Language
import com.example.ludoblitz.model.LevelProgression
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.Quest
import com.example.ludoblitz.model.QuestFactory
import com.example.ludoblitz.service.ArenaLeaderboardService
import com.example.ludoblitz.service.AudioService
import com.example.ludoblitz.service.LeaderboardEntry
import com.example.ludoblitz.service.MusicMode
import com.example.ludoblitz.service.SoundManager
import com.example.ludoblitz.ui.components.CurrencyBurstEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    MATCHMAKING,
    BATTLE,
    BATTLE_RESULT,
    SHOP,
    SEASON_PASS,
    QUESTS,
    LEADERBOARD,
    PROFILE,
    SETTINGS
}

data class LevelUpCelebrationEvent(
    val oldLevel: Int,
    val newLevel: Int,
    val rankTitle: String,
    val badgeEmoji: String,
    val unlockedMilestoneLevel: Int? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(AppDatabase.getDatabase(application))
    private val leaderboardService = ArenaLeaderboardService()
    val audioService = AudioService.getInstance(application)
    val soundManager = SoundManager.getInstance(application)

    val profileFlow: StateFlow<PlayerProfile> = repository.playerProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerProfile())

    val dailyMissionsFlow: StateFlow<List<Quest>> = repository.dailyMissionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuestFactory.getDefaultDailyMissions())

    val weeklyMissionsFlow: StateFlow<List<Quest>> = repository.weeklyMissionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuestFactory.getDefaultWeeklyMissions())

    val achievementsFlow: StateFlow<List<Quest>> = repository.achievementsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuestFactory.getDefaultAchievements())

    val recentMatchesFlow = repository.recentMatchesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedGameFlow = repository.savedGameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen = _currentScreen.asStateFlow()

    private val _leaderboardEntries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardEntries = _leaderboardEntries.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    private val _currencyBurst = MutableStateFlow<CurrencyBurstEvent?>(null)
    val currencyBurst = _currencyBurst.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<LevelUpCelebrationEvent?>(null)
    val levelUpEvent = _levelUpEvent.asStateFlow()

    private val _achievementBanner = MutableStateFlow<Quest?>(null)
    val achievementBanner = _achievementBanner.asStateFlow()

    private val _freeSpinsRemaining = MutableStateFlow(3)
    val freeSpinsRemaining = _freeSpinsRemaining.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultProfileIfNeeded()
        }
        viewModelScope.launch {
            profileFlow.collectLatest { profile ->
                soundManager.updatePreferences(
                    soundEnabled = profile.soundEnabled,
                    musicEnabled = profile.musicEnabled,
                    vibrationEnabled = profile.vibrationEnabled,
                    musicVolume = profile.musicVolume,
                    sfxVolume = profile.sfxVolume
                )
                audioService.setMusicEnabled(profile.musicEnabled)
                audioService.setMusicVolume(profile.musicVolume)
                audioService.setSfxVolume(profile.sfxVolume)
                _leaderboardEntries.value = leaderboardService.getRankedStandings(profile)
            }
        }
    }

    fun onAppPause() {
        audioService.onAppPause()
    }

    fun onAppResume() {
        audioService.onAppResume()
    }

    fun clearLevelUpEvent() {
        _levelUpEvent.value = null
    }

    fun clearAchievementBanner() {
        _achievementBanner.value = null
    }

    fun handleMatchEndResult(result: MatchEndResult) {
        handleXpAwardResult(result.xpAwardResult)
    }

    private fun handleXpAwardResult(xpRes: XpAwardResult?) {
        if (xpRes == null) return
        val profile = profileFlow.value
        if (xpRes.leveledUp) {
            val milestoneHit = LevelProgression.MILESTONES
                .firstOrNull { it.level in (xpRes.oldLevel + 1)..xpRes.newLevel }
                ?.level
            _levelUpEvent.value = LevelUpCelebrationEvent(
                oldLevel = xpRes.oldLevel,
                newLevel = xpRes.newLevel,
                rankTitle = LevelProgression.rankTitleForLevel(xpRes.newLevel),
                badgeEmoji = LevelProgression.badgeForLevel(xpRes.newLevel),
                unlockedMilestoneLevel = milestoneHit
            )
            audioService.playLevelUp(profile.soundEnabled, profile.vibrationEnabled)
        }
        val firstAch = xpRes.newlyCompletedAchievements.firstOrNull()
        if (firstAch != null) {
            _achievementBanner.value = firstAch
            audioService.playAchievementUnlock(profile.soundEnabled, profile.vibrationEnabled)
        }
    }

    fun triggerCurrencyBurst(coins: Int, gems: Int, label: String = "") {
        if (coins <= 0 && gems <= 0) return
        val profile = profileFlow.value
        _currencyBurst.value = CurrencyBurstEvent(
            id = System.nanoTime(),
            coins = coins,
            gems = gems,
            label = label
        )
        audioService.playCoinGemFlyBurst(
            coinCount = if (coins > 0) 5 else 0,
            gemCount = if (gems > 0) 2 else 0,
            soundEnabled = profile.soundEnabled,
            vibrationEnabled = profile.vibrationEnabled
        )
    }

    fun clearCurrencyBurst() {
        _currencyBurst.value = null
    }

    fun navigateTo(screen: AppScreen) {
        val profile = profileFlow.value
        val previous = _currentScreen.value
        if (screen == AppScreen.HOME && previous != AppScreen.HOME) {
            audioService.playBackToHome(profile.soundEnabled, profile.vibrationEnabled)
        } else if (screen != previous) {
            audioService.playTabSwitch(profile.soundEnabled, profile.vibrationEnabled)
        }
        when (screen) {
            AppScreen.BATTLE -> audioService.setMusicMode(MusicMode.BATTLE)
            AppScreen.BATTLE_RESULT -> audioService.setMusicMode(MusicMode.VICTORY)
            else -> audioService.setMusicMode(MusicMode.LOBBY)
        }
        _currentScreen.value = screen
    }

    fun returnHomeWithRewardBurst(coinsEarned: Int, gemsEarned: Int = 0) {
        val profile = profileFlow.value
        _currentScreen.value = AppScreen.HOME
        audioService.setMusicMode(MusicMode.LOBBY)
        audioService.playBackToHome(profile.soundEnabled, profile.vibrationEnabled)
        if (coinsEarned > 0 || gemsEarned > 0) {
            triggerCurrencyBurst(coinsEarned, gemsEarned, "Match Reward!")
        }
    }

    fun playClickSound() {
        val profile = profileFlow.value
        audioService.playButtonClick(profile.soundEnabled, profile.vibrationEnabled)
    }

    fun playChestOpenReward(coins: Int, gems: Int, xpBonus: Int = 35) {
        val profile = profileFlow.value
        viewModelScope.launch {
            repository.addCurrency(coins, gems)
            val xpRes = repository.awardXp(xpBonus)
            handleXpAwardResult(xpRes)
        }
        audioService.playChestReward(profile.soundEnabled, profile.vibrationEnabled)
        triggerCurrencyBurst(coins, gems, "Chest Loot!")
    }

    fun playWheelTick() {
        val profile = profileFlow.value
        audioService.playWheelSpinTick(profile.soundEnabled, false)
    }

    fun claimLuckyWheelReward(coinsWon: Int, gemsWon: Int, spinCostCoins: Int, prizeName: String) {
        val profile = profileFlow.value
        if (spinCostCoins <= 0 && _freeSpinsRemaining.value > 0) {
            _freeSpinsRemaining.value = (_freeSpinsRemaining.value - 1).coerceAtLeast(0)
        }
        viewModelScope.launch {
            val netCoins = coinsWon - spinCostCoins
            if (netCoins != 0 || gemsWon != 0) {
                val updated = profile.copy(
                    coins = (profile.coins + netCoins).coerceAtLeast(0),
                    gems = (profile.gems + gemsWon).coerceAtLeast(0)
                )
                repository.updateProfile(updated)
            }
            val xpRes = repository.awardXp(25)
            handleXpAwardResult(xpRes)
        }
        audioService.playRewardReveal(profile.soundEnabled, profile.vibrationEnabled)
        triggerCurrencyBurst(coinsWon, gemsWon, prizeName)
        showToast("🎡 Lucky Wheel: $prizeName!")
    }

    fun toggleQuickBattleAudio() {
        val current = profileFlow.value
        val nextSound = !current.soundEnabled
        viewModelScope.launch {
            // Only toggle SFX quickly; if turning audio off, also silence music,
            // but never auto-start music unexpectedly if the user had music off.
            val nextMusic = if (!nextSound) false else current.musicEnabled
            audioService.setMusicEnabled(nextMusic)
            repository.updateProfile(
                current.copy(
                    soundEnabled = nextSound,
                    musicEnabled = nextMusic
                )
            )
            if (nextSound) {
                audioService.playButtonClick(true, current.vibrationEnabled)
            }
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun claimDailyReward(day: Int, coins: Int, gems: Int, xp: Int = 35) {
        val profile = profileFlow.value
        viewModelScope.launch {
            val result = repository.claimDailyReward(day, coins, gems, xp)
            if (result.success) {
                audioService.playRewardReveal(profile.soundEnabled, profile.vibrationEnabled)
                triggerCurrencyBurst(result.coinsAwarded, result.gemsAwarded, "Day ${result.claimedDay} Reward!")
                handleXpAwardResult(result.xpAwardResult)
                val resetNote = if (result.resetStreak) " (Streak restarted at Day 1)" else ""
                showToast("🎁 Day ${result.claimedDay}: +${result.coinsAwarded} Coins, +${result.gemsAwarded} Gems, +${result.xpAwarded} XP!$resetNote")
            } else {
                audioService.playError(profile.soundEnabled, profile.vibrationEnabled)
                showToast("⏰ Already claimed today! Come back tomorrow for Day ${result.claimedDay}.")
            }
        }
    }

    fun claimQuest(quest: Quest, isDaily: Boolean) {
        val profile = profileFlow.value
        if (quest.isClaimed || quest.currentProgress < quest.targetProgress) {
            audioService.playError(profile.soundEnabled, profile.vibrationEnabled)
            return
        }
        viewModelScope.launch {
            val result = repository.claimQuestAtomic(quest, isDaily)
            if (result.success) {
                audioService.playRewardReveal(profile.soundEnabled, profile.vibrationEnabled)
                triggerCurrencyBurst(quest.rewardCoins, quest.rewardGems, "Mission Complete!")
                handleXpAwardResult(result.xpAwardResult)
                showToast("🎉 Claimed: +${quest.rewardCoins} Coins, +${quest.rewardGems} Gems, +${quest.rewardXp} XP!")
            } else {
                audioService.playError(profile.soundEnabled, profile.vibrationEnabled)
                showToast("Already claimed!")
            }
        }
    }

    fun claimLevelMilestone(level: Int) {
        val profile = profileFlow.value
        viewModelScope.launch {
            val res = repository.claimLevelMilestone(level)
            if (res.success && res.milestone != null) {
                val m = res.milestone
                audioService.playRewardReveal(profile.soundEnabled, profile.vibrationEnabled)
                triggerCurrencyBurst(m.rewardCoins, m.rewardGems, "Lv.${m.level} ${m.rankTitle}!")
                val bonusText = if (m.rewardCosmeticId != null) " + Exclusive Cosmetic!" else ""
                showToast("🏆 Level ${m.level} Milestone: +${m.rewardCoins} Coins, +${m.rewardGems} Gems$bonusText")
            } else {
                audioService.playError(profile.soundEnabled, profile.vibrationEnabled)
                showToast("Reach Level $level or already claimed!")
            }
        }
    }

    fun buyCosmetic(cosmeticId: String, costCoins: Int, costGems: Int) {
        val profile = profileFlow.value
        viewModelScope.launch {
            val success = repository.unlockCosmetic(cosmeticId, costCoins, costGems)
            if (success) {
                audioService.playPurchase(profile.soundEnabled, profile.vibrationEnabled)
                showToast("✨ Item Unlocked & Added to Collection!")
            } else {
                audioService.playError(profile.soundEnabled, profile.vibrationEnabled)
                val msg = if (profile.unlockedCosmetics.contains(cosmeticId)) {
                    "Already owned!"
                } else {
                    "❌ Not enough Duck Coins or Gems!"
                }
                showToast(msg)
            }
        }
    }

    fun equipCosmetic(cosmeticId: String, type: CosmeticType) {
        val profile = profileFlow.value
        viewModelScope.launch {
            val equipped = repository.equipCosmetic(cosmeticId, type)
            if (equipped) {
                audioService.playItemEquip(profile.soundEnabled, profile.vibrationEnabled)
                showToast("🎨 Equipped successfully!")
            } else {
                audioService.playError(profile.soundEnabled, profile.vibrationEnabled)
            }
        }
    }

    fun setLanguage(language: Language) {
        playClickSound()
        viewModelScope.launch {
            val current = profileFlow.value
            repository.updateProfile(current.copy(languageCode = language.code))
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            val current = profileFlow.value
            repository.updateProfile(current.copy(soundEnabled = enabled))
            if (enabled) {
                audioService.playButtonClick(true, current.vibrationEnabled)
            }
        }
    }

    fun toggleMusic(enabled: Boolean) {
        playClickSound()
        audioService.setMusicEnabled(enabled)
        viewModelScope.launch {
            val current = profileFlow.value
            repository.updateProfile(current.copy(musicEnabled = enabled))
        }
    }

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        audioService.setMusicVolume(clamped)
        soundManager.setMusicVolume(clamped)
        viewModelScope.launch {
            val current = profileFlow.value
            repository.updateProfile(current.copy(musicVolume = clamped))
        }
    }

    fun setSfxVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        audioService.setSfxVolume(clamped)
        soundManager.setSfxVolume(clamped)
        viewModelScope.launch {
            val current = profileFlow.value
            repository.updateProfile(current.copy(sfxVolume = clamped))
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            val current = profileFlow.value
            repository.updateProfile(current.copy(vibrationEnabled = enabled))
            audioService.playButtonClick(current.soundEnabled, enabled)
        }
    }

    fun setMasterMute(muted: Boolean) {
        val current = profileFlow.value
        val nextSound = !muted
        // When muting, turn off music immediately; when unmuting, restore SFX without forcing music ON!
        val nextMusic = if (muted) false else current.musicEnabled
        audioService.setMusicEnabled(nextMusic)
        viewModelScope.launch {
            repository.updateProfile(
                current.copy(
                    soundEnabled = nextSound,
                    musicEnabled = nextMusic,
                    vibrationEnabled = nextSound
                )
            )
            if (nextSound) {
                audioService.playButtonClick(true, true)
            }
        }
    }

    fun completeTutorial() {
        viewModelScope.launch {
            val current = profileFlow.value
            if (!current.isTutorialCompleted) {
                repository.updateProfile(current.copy(isTutorialCompleted = true, coins = current.coins + 150))
                val xpRes = repository.awardXp(50)
                handleXpAwardResult(xpRes)
                triggerCurrencyBurst(150, 0, "Starter Bonus!")
                showToast("🎓 Tutorial Complete! +150 Coins & +50 XP awarded!")
            }
        }
    }

    fun unlockVipPassWithGems(costGems: Int = 100) {
        val current = profileFlow.value
        if (current.isSeasonPassPremium) {
            showToast("VIP Season Pass is already unlocked!")
            return
        }
        if (current.gems < costGems) {
            audioService.playError(current.soundEnabled, current.vibrationEnabled)
            showToast("Need $costGems Gems to unlock VIP Pass! Earn Gems in Quests & Chests.")
            return
        }
        viewModelScope.launch {
            repository.updateProfile(
                current.copy(
                    gems = current.gems - costGems,
                    isSeasonPassPremium = true
                )
            )
            audioService.playPurchase(current.soundEnabled, current.vibrationEnabled)
            showToast("👑 VIP Season Pass Unlocked with $costGems Gems!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioService.onAppPause()
    }
}
