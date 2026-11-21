package com.example.ludoblitz.service

import android.content.Context
import com.example.ludoblitz.model.PowerUpType

/**
 * High-level game sound effect identifiers handled by [SoundManager].
 */
enum class GameSoundEffect {
    DICE_ROLL,
    DICE_SHAKE,
    DICE_LAND,
    ROLL_SIX,
    PAWN_SELECT,
    PAWN_MOVE,
    PAWN_LAND,
    PAWN_LEAVE_BASE,
    PAWN_CAPTURE,
    SAFE_CELL,
    HOME_FINISH,
    EXTRA_TURN,
    POWER_UP,
    TURN_NOTIFICATION,
    MATCH_START,
    MATCH_FOUND,
    VICTORY,
    DEFEAT,
    BUTTON_CLICK,
    TAB_SWITCH,
    BACK_TO_HOME,
    COIN_CLINK,
    GEM_SPARKLE,
    CHEST_REWARD,
    WHEEL_SPIN_TICK,
    REWARD_REVEAL,
    LEVEL_UP,
    ACHIEVEMENT_UNLOCK,
    PURCHASE,
    ITEM_EQUIP,
    ERROR
}

/**
 * Pluggable audio backend abstraction allowing [SoundManager] to delegate to the
 * unified low-latency [AudioService] in production while remaining 100% unit-testable.
 */
interface SoundBackend {
    fun setMusicEnabled(enabled: Boolean)
    fun setMusicMode(mode: MusicMode)
    fun setMusicVolume(volume: Float) {}
    fun setSfxVolume(volume: Float) {}
    fun onAppPause()
    fun onAppResume()
    fun playDiceShake(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playDiceLand(rolledValue: Int, soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playRollSix(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playPawnSelect(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun playTokenStep(stepIndex: Int, totalSteps: Int, soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playTokenLand(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playTokenLeaveBase(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playCapture(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playSafeCell(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playHomeFinish(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playExtraTurn(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun playPowerUp(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playPowerUpType(type: PowerUpType?, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        playPowerUp(soundEnabled, vibrationEnabled)
    }
    fun playButtonClick(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playTabSwitch(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playBackToHome(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playTurnNotification(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playMatchStart(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playMatchFound(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playCountdownBeep(number: Int, soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playVictory(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playDefeat(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playCoinClink(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playGemSparkle(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playCoinGemFlyBurst(coinCount: Int, gemCount: Int, soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playChestReward(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playWheelSpinTick(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun playRewardReveal(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun playLevelUp(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun playAchievementUnlock(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun playPurchase(soundEnabled: Boolean, vibrationEnabled: Boolean)
    fun playItemEquip(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun playError(soundEnabled: Boolean, vibrationEnabled: Boolean) {}
    fun release()
}

private class AudioServiceBackend(private val service: AudioService) : SoundBackend {
    override fun setMusicEnabled(enabled: Boolean) = service.setMusicEnabled(enabled)
    override fun setMusicMode(mode: MusicMode) = service.setMusicMode(mode)
    override fun setMusicVolume(volume: Float) = service.setMusicVolume(volume)
    override fun setSfxVolume(volume: Float) = service.setSfxVolume(volume)
    override fun onAppPause() = service.onAppPause()
    override fun onAppResume() = service.onAppResume()
    override fun playDiceShake(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playDiceShake(soundEnabled, vibrationEnabled)
    override fun playDiceLand(rolledValue: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playDiceLand(rolledValue, soundEnabled, vibrationEnabled)
    override fun playRollSix(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playRollSix(soundEnabled, vibrationEnabled)
    override fun playPawnSelect(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playPawnSelect(soundEnabled, vibrationEnabled)
    override fun playTokenStep(stepIndex: Int, totalSteps: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playTokenStep(stepIndex, totalSteps, soundEnabled, vibrationEnabled)
    override fun playTokenLand(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playTokenLand(soundEnabled, vibrationEnabled)
    override fun playTokenLeaveBase(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playTokenLeaveBase(soundEnabled, vibrationEnabled)
    override fun playCapture(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playCapture(soundEnabled, vibrationEnabled)
    override fun playSafeCell(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playSafeCell(soundEnabled, vibrationEnabled)
    override fun playHomeFinish(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playHomeFinish(soundEnabled, vibrationEnabled)
    override fun playExtraTurn(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playExtraTurn(soundEnabled, vibrationEnabled)
    override fun playPowerUp(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playPowerUp(soundEnabled, vibrationEnabled)
    override fun playPowerUpType(type: PowerUpType?, soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playPowerUp(type, soundEnabled, vibrationEnabled)
    override fun playButtonClick(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playButtonClick(soundEnabled, vibrationEnabled)
    override fun playTabSwitch(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playTabSwitch(soundEnabled, vibrationEnabled)
    override fun playBackToHome(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playBackToHome(soundEnabled, vibrationEnabled)
    override fun playTurnNotification(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playTurnNotification(soundEnabled, vibrationEnabled)
    override fun playMatchStart(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playMatchStart(soundEnabled, vibrationEnabled)
    override fun playMatchFound(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playMatchFound(soundEnabled, vibrationEnabled)
    override fun playCountdownBeep(number: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playCountdownBeep(number, soundEnabled, vibrationEnabled)
    override fun playVictory(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playVictory(soundEnabled, vibrationEnabled)
    override fun playDefeat(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playDefeat(soundEnabled, vibrationEnabled)
    override fun playCoinClink(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playCoinClink(soundEnabled, vibrationEnabled)
    override fun playGemSparkle(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playGemSparkle(soundEnabled, vibrationEnabled)
    override fun playCoinGemFlyBurst(coinCount: Int, gemCount: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playCoinGemFlyBurst(coinCount, gemCount, soundEnabled, vibrationEnabled)
    override fun playChestReward(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playChestReward(soundEnabled, vibrationEnabled)
    override fun playWheelSpinTick(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playWheelSpinTick(soundEnabled, vibrationEnabled)
    override fun playRewardReveal(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playRewardReveal(soundEnabled, vibrationEnabled)
    override fun playLevelUp(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playLevelUp(soundEnabled, vibrationEnabled)
    override fun playAchievementUnlock(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playAchievementUnlock(soundEnabled, vibrationEnabled)
    override fun playPurchase(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playPurchase(soundEnabled, vibrationEnabled)
    override fun playItemEquip(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playItemEquip(soundEnabled, vibrationEnabled)
    override fun playError(soundEnabled: Boolean, vibrationEnabled: Boolean) =
        service.playError(soundEnabled, vibrationEnabled)
    override fun release() = service.release()
}

/**
 * Centralized SoundManager utility for Ludo Blitz game sound effects, haptics, and music state.
 * Background music is OFF by default and only starts when explicitly enabled by the player.
 */
class SoundManager internal constructor(
    private val backend: SoundBackend
) {
    constructor(context: Context) : this(AudioServiceBackend(AudioService.getInstance(context)))

    @Volatile
    var isSoundEnabled: Boolean = true

    @Volatile
    var isVibrationEnabled: Boolean = true

    @Volatile
    var isMusicEnabled: Boolean = false
        private set

    @Volatile
    var musicVolume: Float = 0.7f
        private set

    @Volatile
    var sfxVolume: Float = 1.0f
        private set

    fun updatePreferences(
        soundEnabled: Boolean = this.isSoundEnabled,
        musicEnabled: Boolean = this.isMusicEnabled,
        vibrationEnabled: Boolean = this.isVibrationEnabled,
        musicVolume: Float = this.musicVolume,
        sfxVolume: Float = this.sfxVolume
    ) {
        this.isSoundEnabled = soundEnabled
        this.isVibrationEnabled = vibrationEnabled
        setMusicVolume(musicVolume)
        setSfxVolume(sfxVolume)
        if (this.isMusicEnabled != musicEnabled) {
            this.isMusicEnabled = musicEnabled
            backend.setMusicEnabled(musicEnabled)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        backend.setMusicEnabled(enabled)
    }

    fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        musicVolume = clamped
        backend.setMusicVolume(clamped)
    }

    fun setSfxVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        sfxVolume = clamped
        backend.setSfxVolume(clamped)
    }

    fun setMusicMode(mode: MusicMode) {
        backend.setMusicMode(mode)
    }

    fun onPause() = backend.onAppPause()
    fun onResume() = backend.onAppResume()

    // --- Dice Sound Effects ---

    fun playDiceRoll() = backend.playDiceShake(isSoundEnabled, isVibrationEnabled)
    fun playDiceShake() = backend.playDiceShake(isSoundEnabled, isVibrationEnabled)
    fun playDiceLand(rolledValue: Int) = backend.playDiceLand(rolledValue, isSoundEnabled, isVibrationEnabled)
    fun playRollSix() = backend.playRollSix(isSoundEnabled, isVibrationEnabled)

    // --- Pawn / Token Movement Sound Effects ---

    fun playPawnSelect() = backend.playPawnSelect(isSoundEnabled, isVibrationEnabled)
    fun playPawnMove() = backend.playTokenStep(0, 1, isSoundEnabled, isVibrationEnabled)
    fun playPawnStep(stepIndex: Int, totalSteps: Int) =
        backend.playTokenStep(stepIndex, totalSteps, isSoundEnabled, isVibrationEnabled)
    fun playPawnLand() = backend.playTokenLand(isSoundEnabled, isVibrationEnabled)
    fun playPawnLeaveBase() = backend.playTokenLeaveBase(isSoundEnabled, isVibrationEnabled)
    fun playPawnCapture() = backend.playCapture(isSoundEnabled, isVibrationEnabled)
    fun playSafeCell() = backend.playSafeCell(isSoundEnabled, isVibrationEnabled)
    fun playHomeFinish() = backend.playHomeFinish(isSoundEnabled, isVibrationEnabled)
    fun playExtraTurn() = backend.playExtraTurn(isSoundEnabled, isVibrationEnabled)

    // --- Power-Up Sound Effects (Unique per PowerUpType) ---

    fun playPowerUp(powerUpType: PowerUpType? = null) {
        backend.playPowerUpType(powerUpType, isSoundEnabled, isVibrationEnabled)
        if (powerUpType == PowerUpType.SHIELD || powerUpType == PowerUpType.SAFE_STEP) {
            backend.playSafeCell(isSoundEnabled, false)
        }
    }

    // --- Match, Countdown & Reward Sound Effects ---

    fun playTurnNotification() = backend.playTurnNotification(isSoundEnabled, isVibrationEnabled)
    fun playMatchStart() = backend.playMatchStart(isSoundEnabled, isVibrationEnabled)
    fun playMatchFound() = backend.playMatchFound(isSoundEnabled, isVibrationEnabled)
    fun playCountdownBeep(number: Int) = backend.playCountdownBeep(number, isSoundEnabled, isVibrationEnabled)
    fun playVictory() = backend.playVictory(isSoundEnabled, isVibrationEnabled)
    fun playDefeat() = backend.playDefeat(isSoundEnabled, isVibrationEnabled)
    fun playButtonClick() = backend.playButtonClick(isSoundEnabled, isVibrationEnabled)
    fun playTabSwitch() = backend.playTabSwitch(isSoundEnabled, isVibrationEnabled)
    fun playBackToHome() = backend.playBackToHome(isSoundEnabled, isVibrationEnabled)
    fun playCoinClink() = backend.playCoinClink(isSoundEnabled, isVibrationEnabled)
    fun playGemSparkle() = backend.playGemSparkle(isSoundEnabled, isVibrationEnabled)
    fun playCoinGemBurst(coinCount: Int, gemCount: Int) =
        backend.playCoinGemFlyBurst(coinCount, gemCount, isSoundEnabled, isVibrationEnabled)
    fun playChestReward() = backend.playChestReward(isSoundEnabled, isVibrationEnabled)
    fun playWheelSpinTick() = backend.playWheelSpinTick(isSoundEnabled, false)
    fun playRewardReveal() = backend.playRewardReveal(isSoundEnabled, isVibrationEnabled)
    fun playLevelUp() = backend.playLevelUp(isSoundEnabled, isVibrationEnabled)
    fun playAchievementUnlock() = backend.playAchievementUnlock(isSoundEnabled, isVibrationEnabled)
    fun playPurchase() = backend.playPurchase(isSoundEnabled, isVibrationEnabled)
    fun playItemEquip() = backend.playItemEquip(isSoundEnabled, isVibrationEnabled)
    fun playError() = backend.playError(isSoundEnabled, isVibrationEnabled)

    /**
     * Dispatches any [GameSoundEffect] enum cue using current sound & haptic preferences.
     */
    fun play(effect: GameSoundEffect) {
        when (effect) {
            GameSoundEffect.DICE_ROLL -> playDiceRoll()
            GameSoundEffect.DICE_SHAKE -> playDiceShake()
            GameSoundEffect.DICE_LAND -> playDiceLand(1)
            GameSoundEffect.ROLL_SIX -> playRollSix()
            GameSoundEffect.PAWN_SELECT -> playPawnSelect()
            GameSoundEffect.PAWN_MOVE -> playPawnMove()
            GameSoundEffect.PAWN_LAND -> playPawnLand()
            GameSoundEffect.PAWN_LEAVE_BASE -> playPawnLeaveBase()
            GameSoundEffect.PAWN_CAPTURE -> playPawnCapture()
            GameSoundEffect.SAFE_CELL -> playSafeCell()
            GameSoundEffect.HOME_FINISH -> playHomeFinish()
            GameSoundEffect.EXTRA_TURN -> playExtraTurn()
            GameSoundEffect.POWER_UP -> playPowerUp()
            GameSoundEffect.TURN_NOTIFICATION -> playTurnNotification()
            GameSoundEffect.MATCH_START -> playMatchStart()
            GameSoundEffect.MATCH_FOUND -> playMatchFound()
            GameSoundEffect.VICTORY -> playVictory()
            GameSoundEffect.DEFEAT -> playDefeat()
            GameSoundEffect.BUTTON_CLICK -> playButtonClick()
            GameSoundEffect.TAB_SWITCH -> playTabSwitch()
            GameSoundEffect.BACK_TO_HOME -> playBackToHome()
            GameSoundEffect.COIN_CLINK -> playCoinClink()
            GameSoundEffect.GEM_SPARKLE -> playGemSparkle()
            GameSoundEffect.CHEST_REWARD -> playChestReward()
            GameSoundEffect.WHEEL_SPIN_TICK -> playWheelSpinTick()
            GameSoundEffect.REWARD_REVEAL -> playRewardReveal()
            GameSoundEffect.LEVEL_UP -> playLevelUp()
            GameSoundEffect.ACHIEVEMENT_UNLOCK -> playAchievementUnlock()
            GameSoundEffect.PURCHASE -> playPurchase()
            GameSoundEffect.ITEM_EQUIP -> playItemEquip()
            GameSoundEffect.ERROR -> playError()
        }
    }

    fun release() = backend.release()

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
