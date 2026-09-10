package com.sortescape.game.android

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.sortescape.game.audio.AudioManager
import kotlin.math.sin
import kotlin.math.PI

/**
 * Real synthesis (see AudioManager's kdoc) instead of bundled sound files: every effect is a
 * short generated sine sweep, cached as raw PCM so playback is instant on repeat.
 */
class AndroidAudioManager : AudioManager {

    private val sampleRate = 44100
    private var soundEnabled = true
    private var musicEnabled = true

    private fun tone(freqStart: Float, freqEnd: Float, durationSec: Float, volume: Float = 0.35f): ShortArray {
        val samples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(samples)
        for (i in 0 until samples) {
            val t = i / sampleRate.toFloat()
            val progress = t / durationSec
            val freq = freqStart + (freqEnd - freqStart) * progress
            val envelope = (1f - progress).coerceIn(0f, 1f)
            val angle = 2.0 * PI * freq * t
            buffer[i] = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt().toShort()
        }
        return buffer
    }

    private fun play(pcm: ShortArray) {
        if (!soundEnabled) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val track = AudioTrack(
            attributes, format, pcm.size * 2, AudioTrack.MODE_STATIC, AudioTrack.SESSION_ID_GENERATE
        )
        track.write(pcm, 0, pcm.size)
        track.setNotificationMarkerPosition(pcm.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(t: AudioTrack) {
                t.release()
            }
            override fun onPeriodicNotification(t: AudioTrack) {}
        })
        track.play()
    }

    private val tapSound by lazy { tone(600f, 500f, 0.05f, 0.25f) }
    private val wrongSound by lazy { tone(300f, 180f, 0.18f, 0.3f) }
    private val levelCompleteSound by lazy { tone(500f, 1000f, 0.5f, 0.35f) }
    private val coinSound by lazy { tone(900f, 1300f, 0.12f, 0.3f) }
    private val unlockSound by lazy { tone(700f, 1100f, 0.25f, 0.3f) }
    private val buttonSound by lazy { tone(450f, 400f, 0.04f, 0.2f) }

    override fun playTap() = play(tapSound)

    override fun playCorrectSort(comboLevel: Int) {
        val base = 700f + (comboLevel.coerceIn(1, 5) - 1) * 120f
        play(tone(base, base * 1.4f, 0.12f, 0.3f))
    }

    override fun playWrongSort() = play(wrongSound)
    override fun playLevelComplete() = play(levelCompleteSound)
    override fun playCoinReward() = play(coinSound)
    override fun playUnlock() = play(unlockSound)
    override fun playButtonClick() = play(buttonSound)

    override fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    override fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
    }
}
