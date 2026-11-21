package com.example.ludoblitz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ludoblitz.data.AppDatabase
import com.example.ludoblitz.data.GameRepository
import com.example.ludoblitz.data.MatchEndResult
import com.example.ludoblitz.engine.LudoAIEngine
import com.example.ludoblitz.engine.LudoBlitzEngine
import com.example.ludoblitz.model.AIDifficulty
import com.example.ludoblitz.model.BattleResult
import com.example.ludoblitz.model.ClassicMatchConfig
import com.example.ludoblitz.model.GameBoardConstants
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.model.GameState
import com.example.ludoblitz.model.PowerUpType
import com.example.ludoblitz.model.TokenState
import com.example.ludoblitz.service.AudioService
import com.example.ludoblitz.service.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BattleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(AppDatabase.getDatabase(application))
    private val engine = LudoBlitzEngine()
    private val aiEngine = LudoAIEngine()
    val audioService = AudioService.getInstance(application)
    val soundManager = SoundManager.getInstance(application)

    private val _gameState = MutableStateFlow(engine.initializeGame(GameMode.QUICK_BATTLE, AIDifficulty.MEDIUM))
    val gameState = _gameState.asStateFlow()

    private val _lastMatchEndResult = MutableStateFlow<MatchEndResult?>(null)
    val lastMatchEndResult = _lastMatchEndResult.asStateFlow()

    private var matchTimerJob: Job? = null
    private var aiTurnJob: Job? = null

    var soundEnabled: Boolean
        get() = soundManager.isSoundEnabled
        set(value) {
            soundManager.isSoundEnabled = value
        }

    var vibrationEnabled: Boolean
        get() = soundManager.isVibrationEnabled
        set(value) {
            soundManager.isVibrationEnabled = value
        }

    fun clearLastMatchEndResult() {
        _lastMatchEndResult.value = null
    }

    fun startBattle(mode: GameMode, difficulty: AIDifficulty) {
        matchTimerJob?.cancel()
        aiTurnJob?.cancel()
        _lastMatchEndResult.value = null

        val initial = engine.initializeGame(mode, difficulty)
        _gameState.value = initial
        soundManager.playMatchStart()
        viewModelScope.launch {
            repository.clearSavedGame()
        }
        startTimerLoop()
    }

    fun startClassicGame(config: ClassicMatchConfig, difficulty: AIDifficulty = AIDifficulty.MEDIUM) {
        matchTimerJob?.cancel()
        aiTurnJob?.cancel()
        _lastMatchEndResult.value = null

        val initial = engine.initializeGame(GameMode.CLASSIC, difficulty, config)
        _gameState.value = initial
        soundManager.playMatchStart()
        viewModelScope.launch {
            repository.saveActiveGame(initial)
        }

        if (!initial.isCurrentTurnHuman()) {
            triggerAITurn()
        }
    }

    fun resumeSavedGame(savedState: GameState) {
        matchTimerJob?.cancel()
        aiTurnJob?.cancel()
        _lastMatchEndResult.value = null

        val restoredState = if (savedState.hasRolled && !savedState.isGameOver) {
            val currentTokens = savedState.getTokens(savedState.currentTurn)
            val legal = engine.findLegalMoveTokens(
                tokens = currentTokens,
                side = savedState.currentTurn,
                steps = savedState.diceValue,
                isClassic = savedState.isClassic
            )
            if (legal.isNotEmpty()) {
                savedState.copy(
                    selectableTokenIds = legal,
                    selectedTokenId = if (legal.size == 1) legal.first() else null
                )
            } else {
                savedState.copy(hasRolled = false, selectableTokenIds = emptySet(), selectedTokenId = null)
            }
        } else {
            savedState
        }

        _gameState.value = restoredState
        soundManager.playMatchStart()
        if (!restoredState.isClassic) {
            startTimerLoop()
        } else if (!restoredState.isCurrentTurnHuman()) {
            triggerAITurn()
        }
    }

    private fun startTimerLoop() {
        matchTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _gameState.value
                if (current.isGameOver) {
                    onMatchFinished(current)
                    break
                }

                val nextState = engine.tickMatchTimer(current)
                _gameState.value = nextState

                if (nextState.countdownOverlayNumber != null) {
                    soundManager.playCountdownBeep(nextState.countdownOverlayNumber)
                }

                if (nextState.isGameOver) {
                    onMatchFinished(nextState)
                    break
                }

                if (!nextState.isCurrentTurnHuman() && !nextState.isGameOver && aiTurnJob?.isActive != true) {
                    triggerAITurn()
                }
            }
        }
    }

    fun onPlayerRollDice() {
        val current = _gameState.value
        if (!current.isCurrentTurnHuman() || current.hasRolled || current.isGameOver || current.isRolling) {
            if (!current.isRolling && !current.isGameOver) soundManager.playError()
            return
        }

        viewModelScope.launch {
            val rollingSide = current.currentTurn
            _gameState.value = current.copy(isRolling = true)
            soundManager.playDiceRoll()
            delay(500)

            val rolledState = engine.rollDice(_gameState.value)
            _gameState.value = rolledState
            soundManager.playDiceLand(rolledState.diceValue)

            if (rolledState.isClassic) {
                repository.saveActiveGame(rolledState)
            }

            if (rolledState.currentTurn != rollingSide) {
                soundManager.playError()
                delay(950)
                if (!rolledState.isCurrentTurnHuman() && !rolledState.isGameOver) {
                    triggerAITurn()
                } else if (rolledState.isCurrentTurnHuman() && !rolledState.isGameOver) {
                    soundManager.playTurnNotification()
                }
                return@launch
            }

            if (rolledState.selectableTokenIds.size == 1) {
                delay(420)
                onPlayerSelectToken(rolledState.selectableTokenIds.first())
            } else if (rolledState.selectableTokenIds.isEmpty()) {
                delay(900)
                val passedState = engine.passTurn(_gameState.value)
                _gameState.value = passedState

                if (passedState.isClassic) {
                    repository.saveActiveGame(passedState)
                }

                if (!passedState.isCurrentTurnHuman()) {
                    triggerAITurn()
                } else {
                    soundManager.playTurnNotification()
                }
            }
        }
    }

    fun onPlayerSelectToken(tokenId: Int) {
        val current = _gameState.value
        if (!current.isCurrentTurnHuman() || !current.hasRolled || current.isGameOver) return
        if (!current.selectableTokenIds.contains(tokenId)) {
            soundManager.playError()
            return
        }

        soundManager.playPawnSelect()

        val prevSide = current.currentTurn
        val prevToken = current.allTokens[prevSide]?.find { it.id == tokenId }
        val nextState = engine.moveToken(current, tokenId)
        val nextToken = nextState.allTokens[prevSide]?.find { it.id == tokenId }
        _gameState.value = nextState

        val didCapture = nextState.activeBannerMessage?.contains("CAPTURED") == true
        val didFinish = nextToken?.isFinished == true && prevToken?.isFinished != true
        val didLeaveBase = prevToken?.state == TokenState.BASE && nextToken?.state != TokenState.BASE
        val didSafe = nextToken?.isOnTrack == true && GameBoardConstants.isSafeTile(nextToken.trackPos) &&
            (prevToken?.isOnTrack != true || !GameBoardConstants.isSafeTile(prevToken.trackPos))

        if (didCapture) {
            soundManager.playPawnCapture()
        } else if (didFinish) {
            soundManager.playHomeFinish()
        } else if (didLeaveBase) {
            soundManager.playPawnLeaveBase()
        } else if (didSafe) {
            soundManager.playSafeCell()
        }

        // Play exciting Extra Turn chime if player earned a bonus roll (6, capture, or home)
        if (!nextState.isGameOver && nextState.currentTurn == prevSide && !didCapture && !didFinish) {
            soundManager.playExtraTurn()
        }

        if (nextState.isClassic) {
            viewModelScope.launch {
                repository.saveActiveGame(nextState)
            }
        }

        if (nextState.isGameOver) {
            onMatchFinished(nextState)
        } else if (!nextState.isCurrentTurnHuman()) {
            triggerAITurn()
        }
    }

    fun onPlayerUsePowerUp(powerUp: PowerUpType) {
        val current = _gameState.value
        if (current.isGameOver || current.isClassic) return

        val charges = current.playerPowerUpCharges[powerUp] ?: 0
        if (charges <= 0) {
            soundManager.playError()
            return
        }

        soundManager.playPowerUp(powerUp)
        val updated = engine.activatePowerUp(current, powerUp, current.currentTurn)
        _gameState.value = updated
        if (updated.isGameOver) {
            onMatchFinished(updated)
        }
    }

    private fun triggerAITurn() {
        aiTurnJob?.cancel()
        aiTurnJob = viewModelScope.launch {
            delay(680)
            val stateAfterDelay = _gameState.value
            if (stateAfterDelay.isCurrentTurnHuman() || stateAfterDelay.isGameOver) return@launch

            val aiSide = stateAfterDelay.currentTurn

            if (!stateAfterDelay.isClassic) {
                val powerUpToUse = aiEngine.shouldActivatePowerUp(stateAfterDelay)
                if (powerUpToUse != null) {
                    soundManager.playPowerUp(powerUpToUse)
                    val poweredState = engine.activatePowerUp(stateAfterDelay, powerUpToUse, aiSide)
                    _gameState.value = poweredState
                    if (poweredState.isGameOver) {
                        onMatchFinished(poweredState)
                        return@launch
                    }
                    delay(480)
                }
            }

            _gameState.value = _gameState.value.copy(isRolling = true)
            soundManager.playDiceRoll()
            delay(500)

            val rolledState = engine.rollDice(_gameState.value)
            _gameState.value = rolledState
            soundManager.playDiceLand(rolledState.diceValue)

            if (rolledState.isClassic) {
                repository.saveActiveGame(rolledState)
            }

            if (rolledState.currentTurn != aiSide) {
                delay(950)
                if (!rolledState.isCurrentTurnHuman() && !rolledState.isGameOver) {
                    triggerAITurn()
                } else if (rolledState.isCurrentTurnHuman() && !rolledState.isGameOver) {
                    soundManager.playTurnNotification()
                }
                return@launch
            }

            if (rolledState.selectableTokenIds.isEmpty()) {
                delay(850)
                val passedState = engine.passTurn(_gameState.value)
                _gameState.value = passedState

                if (passedState.isClassic) {
                    repository.saveActiveGame(passedState)
                }

                if (!passedState.isCurrentTurnHuman() && !passedState.isGameOver) {
                    triggerAITurn()
                } else if (passedState.isCurrentTurnHuman() && !passedState.isGameOver) {
                    soundManager.playTurnNotification()
                }
                return@launch
            }

            delay(560)
            val bestTokenId = aiEngine.selectBestMove(rolledState, rolledState.selectableTokenIds, aiSide)
            if (bestTokenId != null) {
                val prevToken = _gameState.value.allTokens[aiSide]?.find { it.id == bestTokenId }
                val movedState = engine.moveToken(_gameState.value, bestTokenId)
                val nextToken = movedState.allTokens[aiSide]?.find { it.id == bestTokenId }
                _gameState.value = movedState

                if (movedState.activeBannerMessage?.contains("CAPTURED") == true) {
                    soundManager.playPawnCapture()
                } else if (nextToken?.isFinished == true && prevToken?.isFinished != true) {
                    soundManager.playHomeFinish()
                } else if (prevToken?.state == TokenState.BASE && nextToken?.state != TokenState.BASE) {
                    soundManager.playPawnLeaveBase()
                } else if (nextToken?.isOnTrack == true && GameBoardConstants.isSafeTile(nextToken.trackPos) && (prevToken?.isOnTrack != true || !GameBoardConstants.isSafeTile(prevToken.trackPos))) {
                    soundManager.playSafeCell()
                }

                if (movedState.isClassic) {
                    repository.saveActiveGame(movedState)
                }

                if (movedState.isGameOver) {
                    onMatchFinished(movedState)
                } else if (!movedState.isCurrentTurnHuman()) {
                    triggerAITurn()
                } else {
                    soundManager.playTurnNotification()
                }
            }
        }
    }

    private fun onMatchFinished(state: GameState) {
        matchTimerJob?.cancel()
        aiTurnJob?.cancel()

        val isWin = state.result == BattleResult.VICTORY

        if (isWin) {
            soundManager.playVictory()
        } else {
            soundManager.playDefeat()
        }

        viewModelScope.launch {
            repository.clearSavedGame()
            val endResult = repository.recordMatchEnd(
                mode = state.mode,
                result = state.result ?: BattleResult.DEFEAT,
                scorePlayer = state.scorePlayer.takeIf { it > 0 } ?: state.totalPlayerScore,
                scoreOpponent = state.scoreOpponent.takeIf { it > 0 } ?: state.totalOpponentScore,
                durationSeconds = if (state.isClassic) 120 else (60 - state.matchTimeRemainingSeconds),
                stats = state.stats,
                winReason = state.winReason
            )
            _lastMatchEndResult.value = endResult
        }
    }

    override fun onCleared() {
        super.onCleared()
        matchTimerJob?.cancel()
        aiTurnJob?.cancel()
    }
}
