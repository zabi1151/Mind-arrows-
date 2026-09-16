package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-quality procedural Audio Engine for Mind Arrows:
 * - Soothing, non-jarring sound effects (gentle water drops, aerodynamic whooshes, crystal chimes)
 * - Calming, low-volume ambient background music (meditative warm chords & subtle pentatonic chimes)
 * - Fully offline & self-contained, with zero harsh telephone beeps
 */
class SoundManager(private val context: Context) {

    private val sampleRate = 44100
    private var isMusicEnabled = true
    private var isSoundEnabled = true
    private var isAppPaused = false

    // Sound effect pre-rendered PCM buffers
    private var clickTrack: AudioTrack? = null
    private var launchTrack: AudioTrack? = null
    private var blockedTrack: AudioTrack? = null
    private var hintTrack: AudioTrack? = null
    private var completeTrack: AudioTrack? = null
    private var coinTrack: AudioTrack? = null

    // Background ambient music engine
    private var bgmTrack: AudioTrack? = null
    private var bgmJob: Job? = null
    private val bgmScope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            initSoundEffects()
            startAmbientMusic()
        } catch (_: Exception) {
            // Safe fallback if audio hardware is restricted
        }
    }

    /* -------------------------------------------------------------------------
     * Sound Effects Synthesis (Warm, Relaxing, Tactile)
     * ------------------------------------------------------------------------- */

    private fun initSoundEffects() {
        try {
            clickTrack = createStaticTrack(generateSoftClick())
            launchTrack = createStaticTrack(generateArrowLaunchSound())
            blockedTrack = createStaticTrack(generateSoftBlockedSound())
            hintTrack = createStaticTrack(generateCrystalHintSound())
            completeTrack = createStaticTrack(generateLevelCompleteChime())
            coinTrack = createStaticTrack(generateCoinChime())
        } catch (_: Exception) {
            // AudioTrack init fallback
        }
    }

    private fun createStaticTrack(pcmData: ShortArray): AudioTrack? {
        return try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcmData.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, pcmData.size)
            track
        } catch (_: Exception) {
            null
        }
    }

    private fun playTrack(track: AudioTrack?) {
        if (!isSoundEnabled || track == null) return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop()
            }
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {
            // Ignore playback error
        }
    }

    /**
     * Soft, organic raindrop/woodblock tap (35ms, 460Hz -> 300Hz, gentle exponential envelope)
     */
    private fun generateSoftClick(): ShortArray {
        val durationMs = 35
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val maxAmp = 8000.0 // Gentle volume (~25% of max 32767)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            // Gentle downward pitch sweep (waterdrop effect)
            val freq = 480.0 - 180.0 * progress
            val wave = sin(2.0 * PI * freq * t)
            // Exponential decay envelope
            val env = exp(-progress * 6.0)
            buffer[i] = (wave * env * maxAmp).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /**
     * Aerodynamic resonant flight whoosh (150ms, rising harmonic with smooth air release)
     */
    private fun generateArrowLaunchSound(): ShortArray {
        val durationMs = 150
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val maxAmp = 10000.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples

            // Rising tone from 260Hz to 520Hz
            val freq = 260.0 + 260.0 * (progress * progress)
            val fundamental = sin(2.0 * PI * freq * t)
            val harmonic2 = 0.3 * sin(4.0 * PI * freq * t)
            val wave = fundamental + harmonic2

            // Smooth attack and soft decay envelope
            val attack = if (progress < 0.15) progress / 0.15 else 1.0
            val decay = exp(-(progress - 0.15).coerceAtLeast(0.0) * 3.5)
            val env = attack * decay

            buffer[i] = (wave * env * maxAmp).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /**
     * Gentle, damped wooden thud (65ms, 160Hz -> 80Hz with heavy damping, never harsh)
     */
    private fun generateSoftBlockedSound(): ShortArray {
        val durationMs = 65
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val maxAmp = 8500.0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples

            val freq = 160.0 - 80.0 * progress
            val wave = sin(2.0 * PI * freq * t)
            val env = exp(-progress * 8.0)

            buffer[i] = (wave * env * maxAmp).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /**
     * Shimmering celestial crystal bell chime (320ms, two harmonic bell frequencies)
     */
    private fun generateCrystalHintSound(): ShortArray {
        val durationMs = 320
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val maxAmp = 9000.0

        val f1 = 784.0 // G5
        val f2 = 1175.0 // D6

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples

            val wave1 = sin(2.0 * PI * f1 * t) * exp(-progress * 3.0)
            val wave2 = 0.6 * sin(2.0 * PI * f2 * t) * exp(-progress * 4.5)
            val wave = wave1 + wave2

            val attack = (progress / 0.05).coerceAtMost(1.0)
            buffer[i] = (wave * attack * maxAmp).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /**
     * Soothing celebratory pentatonic chord arpeggio (700ms, Eb - G - Bb - Eb)
     */
    private fun generateLevelCompleteChime(): ShortArray {
        val durationMs = 700
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val maxAmp = 8000.0

        // Gentle uplifting arpeggio notes in Eb Major pentatonic
        val notes = listOf(
            622.25, // Eb5
            783.99, // G5
            932.33, // Bb5
            1244.50 // Eb6
        )
        val noteSpacing = (sampleRate * 0.09).toInt()

        for (i in 0 until numSamples) {
            var sumWave = 0.0
            for ((noteIdx, noteFreq) in notes.withIndex()) {
                val startSample = noteIdx * noteSpacing
                if (i >= startSample) {
                    val noteSample = i - startSample
                    val t = noteSample.toDouble() / sampleRate
                    val progress = noteSample.toDouble() / (numSamples - startSample)
                    val env = exp(-progress * 3.8)
                    val attack = (progress / 0.02).coerceAtMost(1.0)
                    sumWave += sin(2.0 * PI * noteFreq * t) * env * attack
                }
            }
            buffer[i] = (sumWave * 0.35 * maxAmp).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /**
     * Sweet double coin chime (140ms, C6 -> E6)
     */
    private fun generateCoinChime(): ShortArray {
        val durationMs = 140
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        val maxAmp = 8500.0

        val f1 = 1046.5 // C6
        val f2 = 1318.5 // E6
        val split = numSamples / 2

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val isSecond = i >= split
            val noteFreq = if (isSecond) f2 else f1
            val localSample = if (isSecond) i - split else i
            val progress = localSample.toDouble() / split

            val wave = sin(2.0 * PI * noteFreq * t)
            val env = exp(-progress * 4.0)
            buffer[i] = (wave * env * maxAmp).toInt().coerceIn(-32768, 32767).toShort()
        }
        return buffer
    }

    /* -------------------------------------------------------------------------
     * Relaxing Ambient Background Music Engine (Continuous & Gentle)
     * ------------------------------------------------------------------------- */

    private fun startAmbientMusic() {
        bgmJob?.cancel()
        bgmJob = bgmScope.launch {
            try {
                val bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(sampleRate / 4)

                val track = AudioTrack.Builder()
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
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                bgmTrack = track
                track.play()

                // Meditative, soothing chord progression in F major / D minor:
                // Fmaj7 -> Dm9 -> Bbmaj7 -> Csus4
                val chordFrequencies = listOf(
                    doubleArrayOf(174.61, 220.00, 261.63, 329.63),       // F3, A3, C4, E4 (Fmaj7)
                    doubleArrayOf(146.83, 174.61, 220.00, 261.63, 329.63),// D3, F3, A3, C4, E4 (Dm9)
                    doubleArrayOf(116.54, 146.83, 174.61, 220.00),       // Bb2, D3, F3, A3 (Bbmaj7)
                    doubleArrayOf(130.81, 196.00, 261.63, 293.66)        // C3, G3, C4, D4 (Csus4)
                )

                // Gentle floating pentatonic chimes
                val pentatonicChimes = doubleArrayOf(349.23, 392.00, 440.00, 523.25, 587.33, 659.25) // F4, G4, A4, C5, D5, E5

                val chunkSize = 2048
                val pcmChunk = ShortArray(chunkSize)
                var globalSample = 0L

                // Chord timing: 7 seconds per chord
                val samplesPerChord = sampleRate * 7
                var currentChordIdx = 0

                // Ambient chime trigger tracking
                var nextChimeSample = sampleRate * 3L
                var activeChimeFreq = 0.0
                var chimeStartSample = 0L

                // Master background volume: kept strictly relaxing, soothing, and low (~18%)
                val bgmMasterAmp = 4200.0

                while (isActive) {
                    if (!isMusicEnabled || isAppPaused) {
                        try {
                            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                                track.pause()
                                track.flush()
                            }
                        } catch (_: Exception) {}
                        // Sleep briefly when muted to conserve CPU
                        kotlinx.coroutines.delay(100)
                        continue
                    } else {
                        try {
                            if (track.playState != AudioTrack.PLAYSTATE_PLAYING) {
                                track.play()
                            }
                        } catch (_: Exception) {}
                    }

                    for (i in 0 until chunkSize) {
                        val samplePos = globalSample + i
                        val t = samplePos.toDouble() / sampleRate

                        // Chord progression with smooth cross-fade
                        val chordSamplePos = (samplePos % samplesPerChord).toInt()
                        val chordProgress = chordSamplePos.toDouble() / samplesPerChord
                        val chordIdx = ((samplePos / samplesPerChord) % chordFrequencies.size).toInt()
                        val nextChordIdx = (chordIdx + 1) % chordFrequencies.size

                        val currentNotes = chordFrequencies[chordIdx]
                        val nextNotes = chordFrequencies[nextChordIdx]

                        // Cross-fade window in the last 1.5 seconds of each chord
                        val fadeOutStart = samplesPerChord - sampleRate * 1.5
                        val crossfade = if (chordSamplePos > fadeOutStart) {
                            (chordSamplePos - fadeOutStart) / (sampleRate * 1.5)
                        } else {
                            0.0
                        }

                        // Warm pad synthesis with smooth breathing LFO (tremolo at 0.12 Hz)
                        val breathLfo = 0.85 + 0.15 * sin(2.0 * PI * 0.12 * t)

                        // Current chord sum
                        var chordWave1 = 0.0
                        for (freq in currentNotes) {
                            // Warm fundamental + gentle 2nd harmonic (warm low-pass filter character)
                            chordWave1 += sin(2.0 * PI * freq * t) + 0.25 * sin(4.0 * PI * freq * t)
                        }
                        chordWave1 /= currentNotes.size

                        // Next chord sum
                        var chordWave2 = 0.0
                        if (crossfade > 0.0) {
                            for (freq in nextNotes) {
                                chordWave2 += sin(2.0 * PI * freq * t) + 0.25 * sin(4.0 * PI * freq * t)
                            }
                            chordWave2 /= nextNotes.size
                        }

                        val blendedChord = chordWave1 * (1.0 - crossfade) + chordWave2 * crossfade

                        // Gentle occasional pentatonic ambient chimes
                        var chimeWave = 0.0
                        if (samplePos >= nextChimeSample) {
                            activeChimeFreq = pentatonicChimes[(samplePos / 1000 % pentatonicChimes.size).toInt()]
                            chimeStartSample = samplePos
                            // Schedule next chime between 3.5 to 5.5 seconds later
                            nextChimeSample = samplePos + sampleRate * 4 + (samplePos % 30000).toInt()
                        }

                        if (activeChimeFreq > 0.0 && samplePos >= chimeStartSample) {
                            val chimeTime = (samplePos - chimeStartSample).toDouble() / sampleRate
                            val chimeEnv = exp(-chimeTime * 2.2)
                            if (chimeEnv > 0.01) {
                                chimeWave = (sin(2.0 * PI * activeChimeFreq * t) + 0.3 * sin(4.0 * PI * activeChimeFreq * t)) * chimeEnv * 0.5
                            } else {
                                activeChimeFreq = 0.0
                            }
                        }

                        // Total soothing audio sample
                        val totalSample = (blendedChord * breathLfo + chimeWave) * bgmMasterAmp
                        pcmChunk[i] = totalSample.toInt().coerceIn(-32768, 32767).toShort()
                    }

                    track.write(pcmChunk, 0, chunkSize)
                    globalSample += chunkSize
                }
            } catch (_: Exception) {
                // Background music coroutine handled
            }
        }
    }

    /* -------------------------------------------------------------------------
     * Public Audio Control APIs
     * ------------------------------------------------------------------------- */

    fun playButtonClick(enabled: Boolean = true) {
        if (!enabled || !isSoundEnabled) return
        playTrack(clickTrack)
    }

    fun playArrowLaunch(enabled: Boolean = true) {
        if (!enabled || !isSoundEnabled) return
        playTrack(launchTrack)
    }

    fun playBlocked(enabled: Boolean = true) {
        if (!enabled || !isSoundEnabled) return
        playTrack(blockedTrack)
    }

    fun playHint(enabled: Boolean = true) {
        if (!enabled || !isSoundEnabled) return
        playTrack(hintTrack)
    }

    fun playLevelComplete(enabled: Boolean = true) {
        if (!enabled || !isSoundEnabled) return
        playTrack(completeTrack)
    }

    fun playCoinReward(enabled: Boolean = true) {
        if (!enabled || !isSoundEnabled) return
        playTrack(coinTrack)
    }

    fun setSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        if (!enabled) {
            try {
                listOf(clickTrack, launchTrack, blockedTrack, hintTrack, completeTrack, coinTrack).forEach {
                    if (it?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        it.stop()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            try {
                bgmTrack?.pause()
                bgmTrack?.flush()
            } catch (_: Exception) {}
        } else if (!isAppPaused) {
            try {
                if (bgmTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    bgmTrack?.play()
                }
            } catch (_: Exception) {}
        }
    }

    fun pauseMusic() {
        isAppPaused = true
        try {
            bgmTrack?.pause()
            bgmTrack?.flush()
        } catch (_: Exception) {}
    }

    fun resumeMusic() {
        isAppPaused = false
        if (isMusicEnabled) {
            try {
                bgmTrack?.play()
            } catch (_: Exception) {}
        }
    }

    fun triggerHaptic(enabled: Boolean = true, isError: Boolean = false) {
        if (!enabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (isError) {
                    VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 45), -1)
                } else {
                    VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(if (isError) 60 else 25)
            }
        } catch (_: Exception) {
            // Safe fallback if vibration unavailable
        }
    }

    fun release() {
        try {
            bgmJob?.cancel()
            bgmJob = null

            bgmTrack?.let {
                it.stop()
                it.release()
            }
            bgmTrack = null

            listOf(clickTrack, launchTrack, blockedTrack, hintTrack, completeTrack, coinTrack).forEach {
                it?.let { track ->
                    track.stop()
                    track.release()
                }
            }
            clickTrack = null
            launchTrack = null
            blockedTrack = null
            hintTrack = null
            completeTrack = null
            coinTrack = null
        } catch (_: Exception) {
            // Clean release
        }
    }
}
