package com.example.ludoblitz.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.Process
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import com.example.ludoblitz.model.PowerUpType
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class MusicMode {
    LOBBY,
    BATTLE,
    VICTORY
}

/**
 * Low-Latency Unified Software-Mixed Game Audio & Adaptive Music Engine.
 *
 * Uses a single persistent 44.1 kHz MODE_STREAM AudioTrack mixed in 11.6 ms chunks
 * on a dedicated audio-priority thread so:
 * - Both background music AND short SFX play reliably on all Android devices & emulators
 *   (respecting AudioTrack.getMinBufferSize at all times).
 * - Tap-to-sound latency is ~12–20 ms with zero per-sound AudioTrack creation overhead.
 * - Up to 10 concurrent SFX voices mix cleanly with background music using soft-clipping.
 * - Automatically pauses AudioTrack when idle (0% idle CPU/battery).
 */
class AudioService(private val context: Context) {

    private class ActiveVoice(
        val pcm: ShortArray,
        val gain: Float = 1.0f,
        var cursor: Int = 0
    )

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val sampleRate = 44100
    private val chunkSamples = 512 // ~11.6 ms per mix frame

    // Mixer & lifecycle state
    private val activeVoices = CopyOnWriteArrayList<ActiveVoice>()
    private val maxConcurrentVoices = 10
    private val musicCache = ConcurrentHashMap<MusicMode, ShortArray>()
    private val sfxCache = ConcurrentHashMap<String, ShortArray>()
    private val lastCueTimestamps = ConcurrentHashMap<String, Long>()

    @Volatile private var currentMusicMode: MusicMode = MusicMode.LOBBY
    @Volatile private var activeMusicBuffer: ShortArray? = null
    @Volatile private var musicCursor: Int = 0
    @Volatile private var isMusicEnabledFlag: Boolean = false
    @Volatile private var musicVolume: Float = 0.7f
    @Volatile private var sfxVolume: Float = 1.0f
    @Volatile private var isLifecyclePaused: Boolean = false
    private val isReleased = AtomicBoolean(false)
    private val mixerStarted = AtomicBoolean(false)

    private val wakeLock = Object()
    private var mixerThread: Thread? = null

    private fun ensureMixerStarted() {
        if (isReleased.get()) return
        if (mixerStarted.compareAndSet(false, true)) {
            val thread = Thread({ runMixerLoop() }, "LudoAudioMixer").apply {
                isDaemon = true
            }
            mixerThread = thread
            thread.start()
        }
    }

    private fun wakeMixer() {
        ensureMixerStarted()
        synchronized(wakeLock) {
            wakeLock.notifyAll()
        }
    }

    private fun createStreamAudioTrack(): AudioTrack? {
        return try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return null
            val bufferBytes = maxOf(minBuf * 2, chunkSamples * 2 * 4)

            val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferBytes,
                    AudioTrack.MODE_STREAM
                )
            }

            if (track.state != AudioTrack.STATE_INITIALIZED) {
                track.release()
                null
            } else {
                track
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun runMixerLoop() {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
        } catch (_: Throwable) {
            // Safe fallback in JVM/test environments
        }

        var track: AudioTrack? = null
        val outChunk = ShortArray(chunkSamples)
        var idleFrames = 0
        val maxIdleFramesBeforeSleep = 90 // ~1.0s tail before pausing AudioTrack

        while (!isReleased.get()) {
            val wantsMusic = isMusicEnabledFlag && activeMusicBuffer != null
            val hasVoices = activeVoices.isNotEmpty()
            val shouldPlay = !isLifecyclePaused && (wantsMusic || hasVoices)

            if (!shouldPlay) {
                idleFrames++
                if (isLifecyclePaused || idleFrames >= maxIdleFramesBeforeSleep) {
                    try {
                        track?.let {
                            if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                                it.pause()
                                it.flush()
                            }
                        }
                    } catch (_: Throwable) {}

                    synchronized(wakeLock) {
                        if (!isReleased.get() &&
                            (isLifecyclePaused || (!(isMusicEnabledFlag && activeMusicBuffer != null) && activeVoices.isEmpty()))
                        ) {
                            try {
                                wakeLock.wait(250L)
                            } catch (_: InterruptedException) {
                                return
                            }
                        }
                    }
                    continue
                }
            } else {
                idleFrames = 0
            }

            if (track == null) {
                track = createStreamAudioTrack()
                if (track == null) {
                    // Audio hardware unavailable (e.g., headless unit test); clear voices & wait
                    activeVoices.clear()
                    synchronized(wakeLock) {
                        try {
                            wakeLock.wait(500L)
                        } catch (_: InterruptedException) {
                            return
                        }
                    }
                    continue
                }
            }

            try {
                if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    track.play()
                }
            } catch (_: Throwable) {
                try { track.release() } catch (_: Throwable) {}
                track = null
                continue
            }

            // Mix 1 chunk of music + active SFX voices
            val musicBuf = if (isMusicEnabledFlag && !isLifecyclePaused) activeMusicBuffer else null
            val musicLen = musicBuf?.size ?: 0
            var mCur = musicCursor
            if (musicLen > 0 && mCur >= musicLen) mCur = 0

            val voicesSnapshot = if (activeVoices.isNotEmpty()) activeVoices.toTypedArray() else null
            val baseMusicGain = if (voicesSnapshot != null) 0.30f else 0.42f // gentle ducking when SFX play
            val effectiveMusicGain = baseMusicGain * musicVolume.coerceIn(0f, 1f)
            val effectiveSfxScale = sfxVolume.coerceIn(0f, 1f)

            for (i in 0 until chunkSamples) {
                var sample = 0f

                if (musicBuf != null && musicLen > 0 && effectiveMusicGain > 0.001f) {
                    sample += (musicBuf[mCur] / 32768f) * effectiveMusicGain
                    mCur++
                    if (mCur >= musicLen) mCur = 0
                }

                if (voicesSnapshot != null) {
                    for (v in voicesSnapshot) {
                        val pos = v.cursor
                        if (pos < v.pcm.size) {
                            sample += (v.pcm[pos] / 32768f) * v.gain * effectiveSfxScale
                            v.cursor = pos + 1
                        }
                    }
                }

                // Soft limiter to prevent digital clipping while keeping SFX punchy
                val limited = when {
                    sample > 0.92f -> 0.92f + (sample - 0.92f) * 0.15f
                    sample < -0.92f -> -0.92f + (sample + 0.92f) * 0.15f
                    else -> sample
                }.coerceIn(-0.99f, 0.99f)

                outChunk[i] = (limited * 32767f).toInt().toShort()
            }

            musicCursor = mCur

            if (voicesSnapshot != null) {
                for (v in voicesSnapshot) {
                    if (v.cursor >= v.pcm.size) {
                        activeVoices.remove(v)
                    }
                }
            }

            try {
                val activeTrack = track ?: continue
                val written = activeTrack.write(outChunk, 0, chunkSamples)
                if (written < 0) {
                    try { activeTrack.release() } catch (_: Throwable) {}
                    track = null
                }
            } catch (_: Throwable) {
                try { track?.release() } catch (_: Throwable) {}
                track = null
            }
        }

        try {
            track?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) it.stop()
                it.release()
            }
        } catch (_: Throwable) {}
    }

    private fun canPlayCue(cueKey: String, minIntervalMs: Long = 30L): Boolean {
        val now = System.currentTimeMillis()
        val prev = lastCueTimestamps[cueKey] ?: 0L
        if (now - prev < minIntervalMs) return false
        lastCueTimestamps[cueKey] = now
        return true
    }

    private fun enqueueVoice(pcm: ShortArray, gain: Float = 0.95f) {
        if (isReleased.get() || isLifecyclePaused) return
        while (activeVoices.size >= maxConcurrentVoices) {
            if (activeVoices.isNotEmpty()) activeVoices.removeAt(0) else break
        }
        activeVoices.add(ActiveVoice(pcm, gain))
        wakeMixer()
    }

    // -------------------------
    // ADAPTIVE ARCADE MUSIC
    // -------------------------

    fun setMusicEnabled(enabled: Boolean) {
        val changed = isMusicEnabledFlag != enabled
        isMusicEnabledFlag = enabled
        if (enabled && !isLifecyclePaused) {
            ensureMusicBufferReady(currentMusicMode)
            wakeMixer()
        } else if (changed) {
            wakeMixer()
        }
    }

    fun isMusicEnabled(): Boolean = isMusicEnabledFlag

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
    }

    fun getMusicVolume(): Float = musicVolume

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    fun getSfxVolume(): Float = sfxVolume

    fun setMusicMode(mode: MusicMode) {
        if (currentMusicMode == mode && activeMusicBuffer != null) return
        currentMusicMode = mode
        if (isMusicEnabledFlag) {
            ensureMusicBufferReady(mode)
        }
    }

    fun onAppPause() {
        isLifecyclePaused = true
        activeVoices.clear()
        wakeMixer()
    }

    fun onAppResume() {
        isLifecyclePaused = false
        if (isMusicEnabledFlag) {
            ensureMusicBufferReady(currentMusicMode)
        }
        wakeMixer()
    }

    private fun ensureMusicBufferReady(mode: MusicMode) {
        val cached = musicCache[mode]
        if (cached != null) {
            if (activeMusicBuffer !== cached) {
                activeMusicBuffer = cached
                musicCursor = 0
            }
            wakeMixer()
            return
        }
        scope.launch {
            val built = musicCache.getOrPut(mode) { buildMusicLoop(mode) }
            if (currentMusicMode == mode) {
                activeMusicBuffer = built
                musicCursor = 0
                if (isMusicEnabledFlag && !isLifecyclePaused) {
                    wakeMixer()
                }
            }
        }
    }

    /**
     * Synthesizes a seamless A-B phrase tabletop Ludo & arcade soundtrack
     * (warm marimba melody, kalimba chord pads, plucked bass, woodblock & shaker groove)
     * tailored to Lobby, Battle, or Victory mood at 44.1 kHz.
     */
    private fun buildMusicLoop(mode: MusicMode): ShortArray {
        val stepDur = when (mode) {
            MusicMode.LOBBY -> 0.215   // ~140 BPM warm inviting royal lounge groove
            MusicMode.BATTLE -> 0.185  // ~162 BPM upbeat playful tabletop groove
            MusicMode.VICTORY -> 0.205 // ~146 BPM festive celebratory sparkle
        }
        val numSteps = 64
        val totalSamples = (sampleRate * stepDur * numSteps).toInt()
        val mix = FloatArray(totalSamples)

        // Warm royal tabletop melody (C major pentatonic + joyful lifts)
        val melodyLobby = doubleArrayOf(
            523.25, 0.0, 659.25, 783.99,   1046.50, 0.0, 880.00, 783.99,
            659.25, 0.0, 783.99, 880.00,   783.99, 0.0, 659.25, 587.33,
            523.25, 659.25, 783.99, 0.0,   880.00, 1046.50, 880.00, 783.99,
            698.46, 0.0, 659.25, 587.33,   523.25, 0.0, 392.00, 0.0,
            659.25, 0.0, 783.99, 880.00,   1174.66, 0.0, 1046.50, 880.00,
            783.99, 0.0, 698.46, 659.25,   587.33, 0.0, 783.99, 0.0,
            880.00, 0.0, 1046.50, 1174.66, 1318.51, 0.0, 1046.50, 880.00,
            783.99, 659.25, 587.33, 0.0,   523.25, 0.0, 659.25, 0.0
        )

        // Playful, catchy Ludo board-game battle melody (not overly frantic)
        val melodyBattle = doubleArrayOf(
            523.25, 0.0, 659.25, 783.99,   1046.50, 0.0, 987.77, 783.99,
            698.46, 0.0, 880.00, 783.99,   659.25, 0.0, 587.33, 523.25,
            523.25, 659.25, 783.99, 1046.50, 1174.66, 0.0, 1046.50, 880.00,
            783.99, 698.46, 659.25, 587.33, 783.99, 0.0, 659.25, 0.0,
            659.25, 0.0, 880.00, 1046.50,  1318.51, 0.0, 1174.66, 1046.50,
            880.00, 783.99, 698.46, 659.25, 587.33, 659.25, 783.99, 0.0,
            1046.50, 0.0, 1174.66, 1318.51, 1567.98, 0.0, 1318.51, 1046.50,
            880.00, 783.99, 659.25, 587.33, 523.25, 0.0, 783.99, 0.0
        )

        // Celebratory bright royal melody for Victory
        val melodyVictory = doubleArrayOf(
            523.25, 659.25, 783.99, 1046.50, 1318.51, 0.0, 1174.66, 1046.50,
            880.00, 0.0, 1046.50, 1174.66,   1046.50, 0.0, 783.99, 0.0,
            659.25, 783.99, 1046.50, 1318.51, 1567.98, 0.0, 1318.51, 1174.66,
            1046.50, 880.00, 783.99, 659.25, 783.99, 0.0, 1046.50, 0.0,
            1046.50, 0.0, 1174.66, 1318.51,  1567.98, 0.0, 1318.51, 1046.50,
            1174.66, 0.0, 1046.50, 880.00,   783.99, 0.0, 659.25, 0.0,
            783.99, 880.00, 1046.50, 1318.51, 1567.98, 1318.51, 1046.50, 880.00,
            1046.50, 0.0, 783.99, 0.0,       1046.50, 0.0, 0.0, 0.0
        )

        // Warm harmonic chord progression (C -> F -> G -> C, Am -> F -> G -> C)
        val chordRoots = arrayOf(
            doubleArrayOf(261.63, 329.63, 392.00), // C
            doubleArrayOf(261.63, 329.63, 392.00), // C
            doubleArrayOf(293.66, 349.23, 440.00), // Dm
            doubleArrayOf(196.00, 246.94, 293.66), // G
            doubleArrayOf(261.63, 329.63, 392.00), // C
            doubleArrayOf(220.00, 261.63, 329.63), // Am
            doubleArrayOf(174.61, 220.00, 261.63), // F
            doubleArrayOf(196.00, 246.94, 392.00)  // G
        )

        val bassPattern = doubleArrayOf(
            130.81, 0.0, 196.00, 0.0,  130.81, 0.0, 196.00, 0.0,
            146.83, 0.0, 220.00, 0.0,  196.00, 0.0, 146.83, 0.0,
            130.81, 0.0, 196.00, 0.0,  174.61, 0.0, 220.00, 0.0,
            174.61, 0.0, 146.83, 0.0,  196.00, 0.0, 130.81, 0.0
        )

        val melody = when (mode) {
            MusicMode.LOBBY -> melodyLobby
            MusicMode.BATTLE -> melodyBattle
            MusicMode.VICTORY -> melodyVictory
        }

        val rng = Random(108)
        val masterGain = when (mode) {
            MusicMode.LOBBY -> 0.88
            MusicMode.BATTLE -> 0.94
            MusicMode.VICTORY -> 0.90
        }
        val twoPi = 2.0 * PI

        for (step in 0 until numSteps) {
            val startSample = (step * stepDur * sampleRate).toInt()

            // 1. Warm Wooden Marimba Lead (rich fundamental + woody 4th overtone transient)
            val mFreq = melody[step % melody.size]
            if (mFreq > 0.0) {
                val noteLen = (0.25 * sampleRate).toInt()
                val decay = if (mode == MusicMode.BATTLE) 11.5 else 9.2
                val w1 = twoPi * mFreq / sampleRate
                for (i in 0 until noteLen) {
                    val idx = (startSample + i) % totalSamples
                    val tSec = i.toDouble() / sampleRate
                    val attack = if (i < 160) i / 160.0 else 1.0
                    val env = attack * exp(-decay * tSec)
                    val malletTransient = exp(-48.0 * tSec)
                    val p = w1 * i
                    val wave = sin(p) * 0.68 +
                        sin(p * 2.0) * 0.20 +
                        sin(p * 3.98) * 0.12 * malletTransient
                    mix[idx] += (wave * env * 0.26 * masterGain).toFloat()
                }
            }

            // 2. Gentle Kalimba / Harp Chord Pad on bar downbeats & offbeat sparkles
            if (step % 4 == 0) {
                val chord = chordRoots[(step / 8) % chordRoots.size]
                val padLen = (stepDur * 3.6 * sampleRate).toInt()
                val padGain = if (mode == MusicMode.VICTORY) 0.14 else 0.11
                for (cIdx in chord.indices) {
                    val f = chord[cIdx] * (if (mode == MusicMode.VICTORY) 2.0 else 1.0)
                    val strumOffset = cIdx * (0.018 * sampleRate).toInt()
                    val wc = twoPi * f / sampleRate
                    for (i in 0 until padLen) {
                        val idx = (startSample + strumOffset + i) % totalSamples
                        val tSec = i.toDouble() / sampleRate
                        val attack = if (i < 420) i / 420.0 else 1.0
                        val env = attack * exp(-4.8 * tSec)
                        val wave = sin(wc * i) * 0.80 + sin(wc * i * 2.0) * 0.20
                        mix[idx] += (wave * env * padGain * masterGain).toFloat()
                    }
                }
            }

            // 3. Smooth Plucked Upright Bassline
            val bFreq = bassPattern[step % bassPattern.size]
            if (bFreq > 0.0) {
                val bassLen = (0.30 * sampleRate).toInt()
                val wb = twoPi * bFreq / sampleRate
                for (i in 0 until bassLen) {
                    val idx = (startSample + i) % totalSamples
                    val tSec = i.toDouble() / sampleRate
                    val attack = if (i < 260) i / 260.0 else 1.0
                    val env = attack * exp(-6.8 * tSec)
                    val p = wb * i
                    val wave = sin(p) * 0.84 + sin(p * 2.0) * 0.16
                    mix[idx] += (wave * env * 0.28 * masterGain).toFloat()
                }
            }

            // 4. Tabletop Woodblock / Soft Bongo & Groove Percussion
            if (step % 4 == 0) {
                // Soft warm kick
                val kickLen = (0.085 * sampleRate).toInt()
                val kickVol = if (mode == MusicMode.BATTLE) 0.24 else 0.14
                var kPhase = 0.0
                for (i in 0 until kickLen) {
                    val idx = (startSample + i) % totalSamples
                    val t = i.toDouble() / kickLen
                    val f = 135.0 * exp(-6.5 * t) + 52.0
                    kPhase += twoPi * f / sampleRate
                    val env = exp(-12.5 * t)
                    mix[idx] += (sin(kPhase) * env * kickVol * masterGain).toFloat()
                }
            }
            if (step % 4 == 2) {
                // Playful wooden rim/woodblock accent on backbeat
                val wbLen = (0.042 * sampleRate).toInt()
                val wbFreq = if ((step / 4) % 2 == 0) 760.0 else 880.0
                val wbVol = if (mode == MusicMode.BATTLE) 0.11 else 0.07
                val ww = twoPi * wbFreq / sampleRate
                for (i in 0 until wbLen) {
                    val idx = (startSample + i) % totalSamples
                    val tSec = i.toDouble() / sampleRate
                    val env = exp(-62.0 * tSec)
                    val wave = sin(ww * i) * 0.72 + sin(ww * i * 2.65) * 0.28
                    mix[idx] += (wave * env * wbVol * masterGain).toFloat()
                }
            }
            if (step % 2 == 1) {
                // Soft tabletop shaker
                val hatLen = (0.026 * sampleRate).toInt()
                val hatVol = if (mode == MusicMode.BATTLE) 0.048 else 0.032
                for (i in 0 until hatLen) {
                    val idx = (startSample + i) % totalSamples
                    val tSec = i.toDouble() / sampleRate
                    val env = exp(-115.0 * tSec)
                    val noise = rng.nextDouble() * 2.0 - 1.0
                    mix[idx] += (noise * env * hatVol * masterGain).toFloat()
                }
            }
        }

        val out = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
            out[i] = (mix[i].coerceIn(-0.92f, 0.92f) * 32767f).toInt().toShort()
        }
        return out
    }

    // -------------------------
    // PCM SYNTHESIS HELPERS
    // -------------------------

    /**
     * Synthesizes a realistic carved-wood board-game impact (wooden dice cup rattle,
     * dice landing thud, or wooden pawn tap on a Ludo board).
     */
    private fun synthWoodKnock(
        baseFreq: Double,
        durationMs: Int,
        volume: Float = 0.85f,
        clickAmount: Double = 0.30,
        decayFactor: Double = 9.5
    ): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        val attackSamples = (sampleRate * 0.0012).toInt().coerceAtLeast(1)
        val rng = Random(baseFreq.toInt() * 31 + durationMs)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            // Slight downward resonant pitch bend characteristic of hardwood knocks
            val f1 = baseFreq * (1.0 - 0.24 * t)
            val f2 = baseFreq * 2.74 * (1.0 - 0.16 * t)
            phase1 += 2.0 * PI * f1 / sampleRate
            phase2 += 2.0 * PI * f2 / sampleRate

            val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
            val bodyEnv = attack * exp(-decayFactor * t)
            val clickEnv = exp(-28.0 * t)
            val bodyWave = sin(phase1) * 0.68 + sin(phase2) * 0.32 * exp(-5.0 * t)
            val clickNoise = (rng.nextDouble() * 2.0 - 1.0) * clickAmount * clickEnv
            val sample = (bodyWave * bodyEnv + clickNoise).coerceIn(-1.0, 1.0)
            buffer[i] = (sample * 32767 * volume).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun playWoodKnock(
        baseFreq: Double,
        durationMs: Int,
        volume: Float = 0.85f,
        clickAmount: Double = 0.30,
        decayFactor: Double = 9.5,
        cacheKey: String? = null
    ) {
        val pcm = if (cacheKey != null) {
            sfxCache.getOrPut(cacheKey) {
                synthWoodKnock(baseFreq, durationMs, volume, clickAmount, decayFactor)
            }
        } else {
            synthWoodKnock(baseFreq, durationMs, volume, clickAmount, decayFactor)
        }
        enqueueVoice(pcm)
    }

    private fun synthRichTone(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        decayFactor: Double = 4.0,
        volume: Float = 0.85f,
        harmonic2: Double = 0.35,
        harmonic3: Double = 0.15,
        noiseAmount: Double = 0.0
    ): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        val attackSamples = (sampleRate * 0.003).toInt().coerceAtLeast(1)
        val invNorm = 1.0 / (1.0 + harmonic2 + harmonic3)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val freq = startFreq + (endFreq - startFreq) * t
            phase += 2.0 * PI * freq / sampleRate

            val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
            val envelope = attack * exp(-decayFactor * t)
            var wave = (sin(phase) +
                harmonic2 * sin(phase * 2.0) +
                harmonic3 * sin(phase * 3.0)) * invNorm

            if (noiseAmount > 0.0) {
                wave = wave * (1.0 - noiseAmount) + (Random.nextDouble() * 2.0 - 1.0) * noiseAmount * exp(-8.0 * t)
            }
            buffer[i] = (wave * envelope * 32767 * volume).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun synthChord(
        freqs: List<Double>,
        durationMs: Int,
        decayFactor: Double = 3.0,
        volume: Float = 0.78f
    ): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        val attackSamples = (sampleRate * 0.005).toInt().coerceAtLeast(1)
        val count = freqs.size.coerceAtLeast(1)
        val twoPi = 2.0 * PI

        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val tSec = i.toDouble() / sampleRate
            val attack = if (i < attackSamples) i.toDouble() / attackSamples else 1.0
            val env = attack * exp(-decayFactor * t)
            var sum = 0.0
            for (f in freqs) {
                val p = twoPi * f * tSec
                sum += sin(p) * 0.75 + sin(p * 2.0) * 0.25
            }
            val sample = (sum / count) * env
            buffer[i] = (sample * 32767 * volume).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    private fun playRichTone(
        startFreq: Double,
        endFreq: Double,
        durationMs: Int,
        decayFactor: Double = 4.0,
        volume: Float = 0.85f,
        harmonic2: Double = 0.35,
        harmonic3: Double = 0.15,
        noiseAmount: Double = 0.0,
        cacheKey: String? = null
    ) {
        val pcm = if (cacheKey != null) {
            sfxCache.getOrPut(cacheKey) {
                synthRichTone(startFreq, endFreq, durationMs, decayFactor, volume, harmonic2, harmonic3, noiseAmount)
            }
        } else {
            synthRichTone(startFreq, endFreq, durationMs, decayFactor, volume, harmonic2, harmonic3, noiseAmount)
        }
        enqueueVoice(pcm)
    }

    private fun playChord(
        freqs: List<Double>,
        durationMs: Int,
        decayFactor: Double = 3.0,
        volume: Float = 0.78f,
        cacheKey: String? = null
    ) {
        val pcm = if (cacheKey != null) {
            sfxCache.getOrPut(cacheKey) { synthChord(freqs, durationMs, decayFactor, volume) }
        } else {
            synthChord(freqs, durationMs, decayFactor, volume)
        }
        enqueueVoice(pcm)
    }

    // -------------------------
    // DICE SOUND EFFECTS
    // -------------------------

    /**
     * Authentic wooden dice-cup rattle synchronized to the 500ms dice roll animation
     * (7 crisp organic tabletop knocks tapering naturally into the final settle).
     */
    fun playDiceShake(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("dice_shake", 180L)) {
            scope.launch {
                val knockFreqs = listOf(420.0, 560.0, 470.0, 620.0, 510.0, 590.0, 450.0)
                val knockDelays = listOf(56L, 58L, 62L, 60L, 66L, 68L, 0L)
                for (idx in knockFreqs.indices) {
                    val vol = if (idx == 0) 0.78f else if (idx == knockFreqs.lastIndex) 0.72f else 0.84f
                    playWoodKnock(
                        baseFreq = knockFreqs[idx],
                        durationMs = 38,
                        volume = vol,
                        clickAmount = 0.38,
                        decayFactor = 10.5,
                        cacheKey = "dice_cup_knock_$idx"
                    )
                    if (knockDelays[idx] > 0L) delay(knockDelays[idx])
                }
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 14, 34, 14, 38, 16, 42, 18))
    }

    /**
     * Realistic tabletop dice landing: primary wooden thud + subtle micro-bounce settle,
     * followed by a radiant golden harp fanfare when rolling a SIX!
     */
    fun playDiceLand(rolledValue: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("dice_land", 100L)) {
            scope.launch {
                playWoodKnock(
                    baseFreq = 355.0,
                    durationMs = 72,
                    volume = 0.92f,
                    clickAmount = 0.32,
                    decayFactor = 7.5,
                    cacheKey = "dice_land_main"
                )
                delay(48)
                playWoodKnock(
                    baseFreq = 490.0,
                    durationMs = 42,
                    volume = 0.62f,
                    clickAmount = 0.26,
                    decayFactor = 11.0,
                    cacheKey = "dice_land_bounce"
                )
            }
            if (rolledValue >= 6) {
                playRollSix(soundEnabled = true, vibrationEnabled = false)
            }
        }
        if (vibrationEnabled) vibrate(if (rolledValue >= 6) 55 else 24)
    }

    fun playDiceRoll(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        playDiceShake(soundEnabled, vibrationEnabled)
    }

    fun playRollSix(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("roll_six", 140L)) {
            scope.launch {
                delay(55)
                val harpNotes = listOf(783.99, 987.77, 1174.66, 1567.98)
                for ((idx, note) in harpNotes.withIndex()) {
                    playRichTone(
                        startFreq = note,
                        endFreq = note * 1.015,
                        durationMs = if (idx == harpNotes.lastIndex) 210 else 85,
                        decayFactor = 3.2,
                        volume = 0.80f,
                        harmonic2 = 0.42,
                        harmonic3 = 0.22,
                        cacheKey = "roll_six_harp_$idx"
                    )
                    delay(32)
                }
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 35, 30, 65))
    }

    // -------------------------
    // TOKEN SOUND EFFECTS
    // -------------------------

    /**
     * Warm carved-wood board-game pawn tap with gentle ascending marimba pitch
     * as the token smoothly glides along its path.
     */
    fun playTokenStep(stepIndex: Int, totalSteps: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("token_step", 42L)) {
            val clampedIdx = stepIndex.coerceIn(0, 8)
            val pitchRatio = 1.0 + (clampedIdx * 0.065)
            // Soft wooden tap paired with warm melodic pop
            playWoodKnock(
                baseFreq = 510.0 * pitchRatio,
                durationMs = 44,
                volume = 0.74f,
                clickAmount = 0.18,
                decayFactor = 9.0,
                cacheKey = "pawn_glide_tap_$clampedIdx"
            )
        }
        if (vibrationEnabled && stepIndex == totalSteps - 1) {
            vibrate(14)
        }
    }

    /**
     * Crisp wooden place-down click when a pawn finishes its glide onto the destination square.
     */
    fun playTokenLand(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("token_land", 55L)) {
            playWoodKnock(
                baseFreq = 390.0,
                durationMs = 64,
                volume = 0.84f,
                clickAmount = 0.24,
                decayFactor = 7.8,
                cacheKey = "pawn_place_land"
            )
        }
        if (vibrationEnabled) vibrate(18)
    }

    fun playTokenMove(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        playTokenStep(0, 1, soundEnabled, vibrationEnabled)
    }

    fun playTokenLeaveBase(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("leave_base", 110L)) {
            scope.launch {
                playRichTone(
                    startFreq = 392.0,
                    endFreq = 880.0,
                    durationMs = 95,
                    decayFactor = 3.8,
                    volume = 0.84f,
                    harmonic2 = 0.35,
                    harmonic3 = 0.15,
                    cacheKey = "leave_base_1"
                )
                delay(55)
                playWoodKnock(
                    baseFreq = 587.33,
                    durationMs = 60,
                    volume = 0.82f,
                    clickAmount = 0.22,
                    decayFactor = 7.5,
                    cacheKey = "leave_base_land"
                )
                playChord(
                    listOf(783.99, 1046.50),
                    durationMs = 135,
                    decayFactor = 3.5,
                    volume = 0.78f,
                    cacheKey = "leave_base_2"
                )
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 30, 25, 45))
    }

    /**
     * Classic playful tabletop capture: cartoon WHOOSH -> punchy BONK -> celebratory bonus chime!
     */
    fun playCapture(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("capture", 140L)) {
            scope.launch {
                // Punchy cartoon BONK/POW drop
                playRichTone(
                    startFreq = 820.0,
                    endFreq = 115.0,
                    durationMs = 145,
                    decayFactor = 2.9,
                    volume = 0.96f,
                    harmonic2 = 0.55,
                    harmonic3 = 0.35,
                    noiseAmount = 0.28,
                    cacheKey = "capture_bonk"
                )
                delay(75)
                // Rewarding knock-out celebration chord
                playChord(
                    listOf(523.25, 659.25, 783.99, 1046.50),
                    durationMs = 210,
                    decayFactor = 2.8,
                    volume = 0.90f,
                    cacheKey = "capture_pow"
                )
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 55, 35, 110))
    }

    /**
     * Shimmering star-shield sparkle when landing on a Safe Star cell.
     */
    fun playSafeCell(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("safe_cell", 140L)) {
            scope.launch {
                val freqs = listOf(1046.50, 1318.51, 1567.98, 2093.00)
                for ((idx, f) in freqs.withIndex()) {
                    playRichTone(
                        startFreq = f,
                        endFreq = f * 1.025,
                        durationMs = if (idx == freqs.lastIndex) 130 else 68,
                        decayFactor = 4.2,
                        volume = 0.76f,
                        harmonic2 = 0.45,
                        harmonic3 = 0.25,
                        cacheKey = "safe_$idx"
                    )
                    delay(36)
                }
            }
        }
        if (vibrationEnabled) vibrate(26)
    }

    /**
     * Royal 3-step fanfare + golden sparkle when a pawn reaches the Home Triangle!
     */
    fun playHomeFinish(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("home_finish", 180L)) {
            scope.launch {
                playChord(listOf(523.25, 659.25, 783.99), durationMs = 100, decayFactor = 3.2, volume = 0.84f, cacheKey = "home_1")
                delay(80)
                playChord(listOf(587.33, 880.00, 1174.66), durationMs = 105, decayFactor = 3.0, volume = 0.86f, cacheKey = "home_mid")
                delay(85)
                playChord(listOf(659.25, 783.99, 1046.50, 1567.98), durationMs = 290, decayFactor = 2.1, volume = 0.94f, cacheKey = "home_2")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 50, 40, 120))
    }

    // -------------------------
    // GAME & UI SOUND EFFECTS
    // -------------------------

    fun playButtonClick(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("btn_click", 35L)) {
            playWoodKnock(
                baseFreq = 680.0,
                durationMs = 42,
                volume = 0.70f,
                clickAmount = 0.16,
                decayFactor = 10.0,
                cacheKey = "btn_click_wood"
            )
        }
        if (vibrationEnabled) vibrate(12)
    }

    fun playTabSwitch(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("tab_switch", 55L)) {
            playRichTone(
                startFreq = 660.0,
                endFreq = 990.0,
                durationMs = 54,
                decayFactor = 4.8,
                volume = 0.68f,
                harmonic2 = 0.35,
                harmonic3 = 0.12,
                cacheKey = "tab_switch"
            )
        }
        if (vibrationEnabled) vibrate(12)
    }

    fun playBackToHome(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("back_home", 180L)) {
            scope.launch {
                val notes = listOf(523.25, 659.25, 783.99, 1046.50)
                for ((idx, f) in notes.withIndex()) {
                    playRichTone(
                        startFreq = f,
                        endFreq = f * 1.02,
                        durationMs = if (idx == notes.lastIndex) 155 else 72,
                        decayFactor = 3.2,
                        volume = 0.76f,
                        harmonic2 = 0.4,
                        harmonic3 = 0.2,
                        cacheKey = "back_home_$idx"
                    )
                    delay(42)
                }
            }
        }
        if (vibrationEnabled) vibrate(20)
    }

    /**
     * Warm friendly 2-note marimba chime signaling it is your turn to roll!
     */
    fun playTurnNotification(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("turn_notice", 220L)) {
            scope.launch {
                playRichTone(
                    startFreq = 783.99,
                    endFreq = 783.99,
                    durationMs = 72,
                    decayFactor = 4.8,
                    volume = 0.70f,
                    harmonic2 = 0.32,
                    harmonic3 = 0.10,
                    cacheKey = "turn_ding_1"
                )
                delay(65)
                playRichTone(
                    startFreq = 1046.50,
                    endFreq = 1046.50,
                    durationMs = 115,
                    decayFactor = 3.8,
                    volume = 0.76f,
                    harmonic2 = 0.35,
                    harmonic3 = 0.12,
                    cacheKey = "turn_ding_2"
                )
            }
        }
        if (vibrationEnabled) vibrate(16)
    }

    fun playMatchStart(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("match_start", 280L)) {
            scope.launch {
                playChord(listOf(523.25, 659.25), durationMs = 90, decayFactor = 3.5, volume = 0.84f, cacheKey = "match_start_1")
                delay(72)
                playChord(listOf(659.25, 783.99, 1046.50), durationMs = 200, decayFactor = 2.4, volume = 0.90f, cacheKey = "match_start_2")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 40, 35, 75))
    }

    fun playMatchFound(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("match_found", 280L)) {
            scope.launch {
                playRichTone(
                    startFreq = 260.0,
                    endFreq = 90.0,
                    durationMs = 135,
                    decayFactor = 3.0,
                    volume = 0.92f,
                    harmonic2 = 0.5,
                    noiseAmount = 0.25,
                    cacheKey = "match_found_1"
                )
                delay(75)
                playChord(listOf(523.25, 783.99, 1046.50), durationMs = 220, decayFactor = 2.4, volume = 0.88f, cacheKey = "match_found_2")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 60, 40, 80))
    }

    fun playPowerUp(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        playPowerUp(null, soundEnabled, vibrationEnabled)
    }

    /**
     * Distinct, high-energy arcade signature sound per Blitz Power-Up type.
     */
    fun playPowerUp(type: PowerUpType?, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("power_up_${type?.name ?: "GEN"}", 130L)) {
            scope.launch {
                when (type) {
                    PowerUpType.SPEED_BOOST -> {
                        playRichTone(320.0, 1480.0, 115, 2.0, 0.88f, 0.45, 0.2, cacheKey = "pu_speed_1")
                        delay(55)
                        playRichTone(880.0, 2100.0, 120, 2.4, 0.90f, 0.4, 0.25, cacheKey = "pu_speed_2")
                    }
                    PowerUpType.SHIELD -> {
                        playChord(listOf(523.25, 783.99, 1046.50), 110, 3.0, 0.82f, cacheKey = "pu_shield_1")
                        delay(65)
                        playChord(listOf(659.25, 987.77, 1318.51, 1975.53), 185, 2.6, 0.86f, cacheKey = "pu_shield_2")
                    }
                    PowerUpType.DOUBLE_DICE -> {
                        playWoodKnock(540.0, 42, 0.85f, 0.30, 9.5, cacheKey = "pu_double_1")
                        delay(58)
                        playWoodKnock(720.0, 46, 0.88f, 0.30, 9.0, cacheKey = "pu_double_2")
                        delay(52)
                        playChord(listOf(880.0, 1318.51), 130, 3.2, 0.84f, cacheKey = "pu_double_3")
                    }
                    PowerUpType.EXTRA_TURN -> {
                        val notes = listOf(659.25, 783.99, 1046.50, 1318.51)
                        for ((idx, f) in notes.withIndex()) {
                            playRichTone(f, f * 1.02, 85, 3.2, 0.82f, 0.4, 0.2, cacheKey = "pu_extra_$idx")
                            delay(42)
                        }
                    }
                    PowerUpType.SAFE_STEP -> {
                        playRichTone(783.99, 1046.50, 95, 3.4, 0.80f, 0.38, 0.18, cacheKey = "pu_safe_1")
                        delay(60)
                        playChord(listOf(1046.50, 1318.51, 1567.98), 165, 2.9, 0.84f, cacheKey = "pu_safe_2")
                    }
                    PowerUpType.BLITZ_MOVE -> {
                        playRichTone(1400.0, 420.0, 75, 2.8, 0.88f, 0.5, 0.3, noiseAmount = 0.22, cacheKey = "pu_blitz_1")
                        delay(50)
                        playRichTone(480.0, 1960.0, 135, 2.2, 0.92f, 0.45, 0.25, cacheKey = "pu_blitz_2")
                    }
                    null -> {
                        playRichTone(340.0, 1100.0, 110, 2.2, 0.88f, 0.45, 0.2, cacheKey = "powerup_1")
                        delay(65)
                        playRichTone(880.0, 1660.0, 130, 2.8, 0.92f, 0.4, 0.2, cacheKey = "powerup_2")
                    }
                }
            }
        }
        if (vibrationEnabled) vibrate(50)
    }

    fun playCountdownBeep(number: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("countdown_$number", 220L)) {
            if (number == 1) {
                playChord(listOf(880.0, 1174.66, 1318.51), durationMs = 175, decayFactor = 2.8, volume = 0.92f, cacheKey = "countdown_1")
            } else {
                playRichTone(startFreq = 880.0, endFreq = 960.0, durationMs = 80, decayFactor = 3.8, volume = 0.88f, cacheKey = "countdown_tick")
            }
        }
        if (vibrationEnabled) vibrate(if (number == 1) 105 else 40)
    }

    fun playVictory(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("victory", 450L)) {
            scope.launch {
                val notes = listOf(523.25, 659.25, 783.99, 1046.50, 987.77, 1318.51, 1567.98)
                for ((idx, note) in notes.withIndex()) {
                    playRichTone(
                        startFreq = note,
                        endFreq = note,
                        durationMs = if (idx == notes.lastIndex) 280 else 96,
                        decayFactor = 2.2,
                        volume = 0.88f,
                        harmonic2 = 0.45,
                        harmonic3 = 0.25,
                        cacheKey = "victory_$idx"
                    )
                    delay(76)
                }
                playChord(
                    listOf(523.25, 659.25, 783.99, 1046.50, 1567.98),
                    durationMs = 340,
                    decayFactor = 1.9,
                    volume = 0.90f,
                    cacheKey = "victory_finale_chord"
                )
                delay(110)
                playCoinGemFlyBurst(coinCount = 5, gemCount = 2, soundEnabled = true, vibrationEnabled = false)
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 70, 45, 80, 45, 160))
    }

    fun playDefeat(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("defeat", 450L)) {
            scope.launch {
                val notes = listOf(440.0, 415.30, 392.0, 329.63)
                for ((idx, note) in notes.withIndex()) {
                    val isLast = idx == notes.lastIndex
                    playRichTone(
                        startFreq = note,
                        endFreq = note * (if (isLast) 0.84 else 0.94),
                        durationMs = if (isLast) 260 else 130,
                        decayFactor = if (isLast) 1.8 else 2.6,
                        volume = 0.80f,
                        harmonic2 = 0.45,
                        harmonic3 = 0.22,
                        cacheKey = "defeat_$idx"
                    )
                    delay(125)
                }
            }
        }
        if (vibrationEnabled) vibrate(95)
    }

    // -------------------------
    // REWARD SOUND EFFECTS
    // -------------------------

    fun playCoinClink(soundEnabled: Boolean, vibrationEnabled: Boolean = false) {
        if (soundEnabled && canPlayCue("coin_clink", 40L)) {
            playChord(listOf(1975.53, 2637.02, 3951.07), durationMs = 105, decayFactor = 5.2, volume = 0.80f, cacheKey = "coin_clink")
        }
        if (vibrationEnabled) vibrate(16)
    }

    fun playSpinningCoin(soundEnabled: Boolean, vibrationEnabled: Boolean = false) {
        if (soundEnabled && canPlayCue("coin_spin", 80L)) {
            playRichTone(startFreq = 2200.0, endFreq = 2850.0, durationMs = 62, decayFactor = 5.0, volume = 0.68f, harmonic2 = 0.4, cacheKey = "coin_spin")
        }
        if (vibrationEnabled) vibrate(12)
    }

    fun playGemSparkle(soundEnabled: Boolean, vibrationEnabled: Boolean = false) {
        if (soundEnabled && canPlayCue("gem_sparkle", 110L)) {
            scope.launch {
                playRichTone(startFreq = 1760.0, endFreq = 2349.32, durationMs = 76, decayFactor = 3.5, volume = 0.78f, harmonic2 = 0.5, harmonic3 = 0.3, cacheKey = "gem_1")
                delay(36)
                playRichTone(startFreq = 2637.02, endFreq = 3520.0, durationMs = 100, decayFactor = 3.8, volume = 0.84f, harmonic2 = 0.5, harmonic3 = 0.3, cacheKey = "gem_2")
            }
        }
        if (vibrationEnabled) vibrate(20)
    }

    fun playCoinGemFlyBurst(
        coinCount: Int,
        gemCount: Int,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean = true
    ) {
        if (soundEnabled && canPlayCue("currency_burst", 200L)) {
            scope.launch {
                if (coinCount > 0) {
                    playSpinningCoin(soundEnabled = true, vibrationEnabled = false)
                    delay(55)
                }
                val clinks = coinCount.coerceIn(0, 6)
                repeat(clinks) { i ->
                    val pitchShift = 1.0 + (i * 0.045)
                    playChord(
                        listOf(1975.53 * pitchShift, 2637.02 * pitchShift),
                        durationMs = 88,
                        decayFactor = 5.5,
                        volume = 0.76f,
                        cacheKey = "coin_fly_$i"
                    )
                    delay(75)
                }
                if (gemCount > 0) {
                    delay(30)
                    playGemSparkle(soundEnabled = true, vibrationEnabled = false)
                }
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 22, 30, 22, 30, 40))
    }

    fun playChestReward(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("chest_reward", 260L)) {
            scope.launch {
                val freqs = listOf(440.0, 554.37, 659.25, 880.0, 1108.73, 1318.51)
                for ((idx, f) in freqs.withIndex()) {
                    playRichTone(
                        startFreq = f,
                        endFreq = f * 1.02,
                        durationMs = 85,
                        decayFactor = 3.2,
                        volume = 0.84f,
                        harmonic2 = 0.45,
                        harmonic3 = 0.2,
                        cacheKey = "chest_$idx"
                    )
                    delay(44)
                }
                delay(50)
                playCoinGemFlyBurst(coinCount = 4, gemCount = 1, soundEnabled = true, vibrationEnabled = false)
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 40, 30, 70, 30, 110))
    }

    fun playPurchase(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("purchase", 220L)) {
            scope.launch {
                playChord(listOf(1318.51, 1661.22), durationMs = 82, decayFactor = 4.2, volume = 0.86f, cacheKey = "purchase_1")
                delay(54)
                playChord(listOf(1760.0, 2217.46, 2637.02), durationMs = 165, decayFactor = 2.6, volume = 0.90f, cacheKey = "purchase_2")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 35, 40, 80))
    }

    // -------------------------
    // NEW ACTION & PROGRESSION SFX
    // -------------------------

    fun playPawnSelect(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("pawn_select", 50L)) {
            playRichTone(
                startFreq = 620.0,
                endFreq = 980.0,
                durationMs = 58,
                decayFactor = 5.2,
                volume = 0.78f,
                harmonic2 = 0.32,
                harmonic3 = 0.12,
                cacheKey = "pawn_select"
            )
        }
        if (vibrationEnabled) vibrate(15)
    }

    fun playExtraTurn(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("extra_turn", 180L)) {
            scope.launch {
                val notes = listOf(659.25, 880.0, 1174.66, 1567.98)
                for ((idx, f) in notes.withIndex()) {
                    playRichTone(
                        startFreq = f,
                        endFreq = f * 1.02,
                        durationMs = if (idx == notes.lastIndex) 165 else 72,
                        decayFactor = 3.2,
                        volume = 0.82f,
                        harmonic2 = 0.42,
                        harmonic3 = 0.18,
                        cacheKey = "extra_turn_$idx"
                    )
                    delay(44)
                }
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 25, 25, 45))
    }

    fun playWheelSpinTick(soundEnabled: Boolean, vibrationEnabled: Boolean = false) {
        if (soundEnabled && canPlayCue("wheel_tick", 28L)) {
            playWoodKnock(
                baseFreq = 840.0,
                durationMs = 28,
                volume = 0.74f,
                clickAmount = 0.34,
                decayFactor = 13.5,
                cacheKey = "wheel_peg_tick"
            )
        }
        if (vibrationEnabled) vibrate(8)
    }

    fun playRewardReveal(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("reward_reveal", 240L)) {
            scope.launch {
                playChord(listOf(523.25, 659.25, 783.99), 95, 3.4, 0.82f, cacheKey = "reward_rev_1")
                delay(72)
                playChord(listOf(659.25, 987.77, 1318.51), 115, 3.0, 0.86f, cacheKey = "reward_rev_2")
                delay(85)
                playChord(listOf(783.99, 1046.50, 1567.98, 2093.00), 250, 2.2, 0.90f, cacheKey = "reward_rev_3")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 35, 30, 65, 30, 90))
    }

    fun playLevelUp(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("level_up", 350L)) {
            scope.launch {
                val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98)
                for ((idx, f) in notes.withIndex()) {
                    playRichTone(
                        startFreq = f,
                        endFreq = f * 1.01,
                        durationMs = if (idx == notes.lastIndex) 260 else 86,
                        decayFactor = 2.6,
                        volume = 0.86f,
                        harmonic2 = 0.45,
                        harmonic3 = 0.22,
                        cacheKey = "lvl_up_$idx"
                    )
                    delay(62)
                }
                playChord(listOf(523.25, 783.99, 1046.50, 1567.98), 320, 2.0, 0.92f, cacheKey = "lvl_up_chord")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 50, 35, 70, 35, 130))
    }

    fun playAchievementUnlock(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("ach_unlock", 280L)) {
            scope.launch {
                playRichTone(880.0, 1174.66, 90, 3.4, 0.82f, 0.42, 0.2, cacheKey = "ach_1")
                delay(65)
                playChord(listOf(1046.50, 1318.51, 1760.0), 210, 2.5, 0.88f, cacheKey = "ach_2")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 35, 30, 75))
    }

    fun playItemEquip(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("item_equip", 90L)) {
            scope.launch {
                playWoodKnock(760.0, 36, 0.78f, 0.25, 11.0, cacheKey = "equip_1")
                delay(42)
                playRichTone(987.77, 1318.51, 90, 4.0, 0.80f, 0.36, 0.15, cacheKey = "equip_2")
            }
        }
        if (vibrationEnabled) vibrate(20)
    }

    fun playError(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled && canPlayCue("soft_error", 140L)) {
            scope.launch {
                playRichTone(280.0, 220.0, 78, 4.5, 0.72f, 0.3, 0.1, cacheKey = "err_1")
                delay(68)
                playRichTone(215.0, 175.0, 95, 4.2, 0.70f, 0.3, 0.1, cacheKey = "err_2")
            }
        }
        if (vibrationEnabled) vibratePattern(longArrayOf(0, 25, 35, 25))
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        isReleased.set(true)
        activeVoices.clear()
        wakeMixer()
        scope.cancel()
    }

    companion object {
        @Volatile
        private var instance: AudioService? = null

        fun getInstance(context: Context): AudioService {
            return instance ?: synchronized(this) {
                instance ?: AudioService(context.applicationContext).also { instance = it }
            }
        }
    }
}
