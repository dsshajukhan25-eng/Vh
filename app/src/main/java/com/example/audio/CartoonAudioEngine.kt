package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import com.example.model.CartoonBgmTrack
import com.example.model.CartoonSfx
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

/**
 * CartoonAudioEngine produces procedural cartoon music, sound effects,
 * and text-to-speech voiceovers for ToonMaker AI.
 */
class CartoonAudioEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var currentBgmTrack: AudioTrack? = null
    private var isBgmPlaying = false
    private var bgmThread: Thread? = null

    // Volume levels (0.0f - 1.0f)
    var bgmVolume: Float = 0.75f
    var sfxVolume: Float = 0.90f
    var voiceVolume: Float = 1.0f

    var activeBgmTrack: CartoonBgmTrack = CartoonBgmTrack.PLAYFUL_BOUNCE

    // TTS Pitch & Speed
    var ttsPitch: Float = 1.6f // Higher pitch gives a cute cartoon effect
    var ttsSpeed: Float = 1.1f

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            // Try Bengali first, then default locale
            val result = tts?.setLanguage(Locale("bn", "BD"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }
            tts?.setPitch(ttsPitch)
            tts?.setSpeechRate(ttsSpeed)
        }
    }

    /**
     * Start playing procedural cartoon BGM in a loop.
     */
    fun startBgm(track: CartoonBgmTrack) {
        stopBgm()
        activeBgmTrack = track
        isBgmPlaying = true

        bgmThread = thread(start = true, name = "CartoonBgmThread") {
            val sampleRate = 22050
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufSize * 2).coerceAtLeast(sampleRate)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            currentBgmTrack = audioTrack
            try {
                audioTrack.play()
            } catch (e: Exception) {
                return@thread
            }

            // Note melody frequencies for C Major / Pentatonic cartoon scales
            val notes = when (track) {
                CartoonBgmTrack.PLAYFUL_BOUNCE -> doubleArrayOf(
                    261.63, 329.63, 392.00, 523.25, 392.00, 329.63,
                    293.66, 349.23, 440.00, 587.33, 440.00, 349.23
                )
                CartoonBgmTrack.SLAPSTICK_COMEDY -> doubleArrayOf(
                    196.00, 246.94, 329.63, 392.00, 493.88, 392.00,
                    220.00, 277.18, 349.23, 440.00, 554.37, 440.00
                )
                CartoonBgmTrack.CIRCUS_FUN -> doubleArrayOf(
                    329.63, 392.00, 493.88, 587.33, 659.25, 587.33,
                    293.66, 369.99, 440.00, 554.37, 659.25, 440.00
                )
                CartoonBgmTrack.UPBEAT_GROOVE -> doubleArrayOf(
                    220.00, 261.63, 329.63, 440.00, 523.25, 440.00,
                    174.61, 220.00, 261.63, 349.23, 440.00, 349.23
                )
                CartoonBgmTrack.MAGIC_WONDER -> doubleArrayOf(
                    523.25, 659.25, 783.99, 1046.50, 783.99, 659.25,
                    587.33, 698.46, 880.00, 1174.66, 880.00, 698.46
                )
            }

            val noteDurationSeconds = 0.22
            val noteSamples = (sampleRate * noteDurationSeconds).toInt()
            val shortBuffer = ShortArray(noteSamples)

            var noteIndex = 0

            while (isBgmPlaying && !Thread.currentThread().isInterrupted) {
                val freq = notes[noteIndex % notes.size]
                val baseVol = (32767 * 0.28f * bgmVolume).toInt()

                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / sampleRate
                    // Envelope attack-decay
                    val envelope = if (i < noteSamples * 0.15) {
                        (i.toDouble() / (noteSamples * 0.15))
                    } else {
                        1.0 - (i.toDouble() / noteSamples) * 0.7
                    }
                    // Cartoon bouncy waveform (Fundamental + 2nd harmonic + sub bass)
                    val sampleVal = (sin(2.0 * PI * freq * t) * 0.7 +
                            sin(2.0 * PI * (freq * 2.0) * t) * 0.3 +
                            sin(2.0 * PI * (freq * 0.5) * t) * 0.2) * envelope

                    shortBuffer[i] = (sampleVal * baseVol).toInt().coerceIn(-32767, 32767).toShort()
                }

                try {
                    audioTrack.write(shortBuffer, 0, noteSamples)
                } catch (e: Exception) {
                    break
                }

                noteIndex++
            }

            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }

    /**
     * Stop playing BGM.
     */
    fun stopBgm() {
        isBgmPlaying = false
        bgmThread?.interrupt()
        bgmThread = null
        try {
            currentBgmTrack?.stop()
            currentBgmTrack?.release()
        } catch (e: Exception) {
            // Ignore
        }
        currentBgmTrack = null
    }

    /**
     * Play one of the fun cartoon sound effects.
     */
    fun playSfx(sfx: CartoonSfx) {
        thread(start = true, name = "CartoonSfxThread") {
            val sampleRate = 22050
            val durationSeconds = when (sfx) {
                CartoonSfx.BOING -> 0.45
                CartoonSfx.POP -> 0.12
                CartoonSfx.WHISTLE -> 0.55
                CartoonSfx.DING -> 0.70
                CartoonSfx.FANFARE -> 0.65
            }
            val totalSamples = (sampleRate * durationSeconds).toInt()
            val shortBuffer = ShortArray(totalSamples)
            val baseVol = (32767 * 0.65f * sfxVolume).toInt()

            for (i in 0 until totalSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / totalSamples

                val sample = when (sfx) {
                    CartoonSfx.BOING -> {
                        // Rapid upward frequency slide with bounce vibration
                        val f = 180.0 + progress * 750.0 + sin(progress * 60.0) * 80.0
                        sin(2.0 * PI * f * t) * (1.0 - progress)
                    }
                    CartoonSfx.POP -> {
                        // Sudden pressure wave pop
                        val f = 400.0 - progress * 280.0
                        sin(2.0 * PI * f * t) * (1.0 - progress)
                    }
                    CartoonSfx.WHISTLE -> {
                        // Cartoon slide whistle glissando
                        val f = 800.0 + sin(progress * PI) * 600.0
                        sin(2.0 * PI * f * t) * (1.0 - progress * 0.4)
                    }
                    CartoonSfx.DING -> {
                        // High chime chime
                        val f = 1760.0
                        (sin(2.0 * PI * f * t) + 0.4 * sin(2.0 * PI * (f * 2) * t)) * (1.0 - progress)
                    }
                    CartoonSfx.FANFARE -> {
                        // Quick trumpet arpeggio
                        val noteFreq = when {
                            progress < 0.25 -> 523.25
                            progress < 0.50 -> 659.25
                            progress < 0.75 -> 783.99
                            else -> 1046.50
                        }
                        sin(2.0 * PI * noteFreq * t) * 0.8
                    }
                }

                shortBuffer[i] = (sample * baseVol).toInt().coerceIn(-32767, 32767).toShort()
            }

            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .build()
                    )
                    .setBufferSizeInBytes(totalSamples * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(shortBuffer, 0, totalSamples)
                audioTrack.play()
                // Sleep until finished, then release
                Thread.sleep((durationSeconds * 1000).toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Speak cartoon voiceover via Text-to-Speech.
     */
    fun speakCartoonVoice(text: String, pitchMultiplier: Float = 1.6f) {
        if (!isTtsReady || text.isBlank()) return
        tts?.setPitch(pitchMultiplier)
        tts?.setSpeechRate(ttsSpeed)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "CartoonTTS")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun release() {
        stopBgm()
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
