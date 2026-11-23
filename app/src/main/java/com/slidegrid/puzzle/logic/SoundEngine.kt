package com.slidegrid.puzzle.logic

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.slidegrid.puzzle.R

class SoundEngine(context: Context) {

    private var soundPool: SoundPool? = null
    private var slideSoundId: Int = 0
    private var placeSoundId: Int = 0
    private var victorySoundId: Int = 0
    private var isReleased = false

    init {
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attributes)
                .build()

            slideSoundId = pool.load(context, R.raw.sfx_slide, 1)
            placeSoundId = pool.load(context, R.raw.sfx_place, 1)
            victorySoundId = pool.load(context, R.raw.sfx_victory, 1)
            soundPool = pool
        } catch (_: Throwable) {
            soundPool = null
        }
    }

    fun playSlide(enabled: Boolean, placedInTarget: Boolean) {
        if (!enabled || isReleased) return
        val id = if (placedInTarget) placeSoundId else slideSoundId
        playSafe(id, 0.75f)
    }

    fun playVictory(enabled: Boolean) {
        if (!enabled || isReleased) return
        playSafe(victorySoundId, 0.9f)
    }

    private fun playSafe(soundId: Int, volume: Float) {
        if (soundId == 0) return
        try {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (_: Throwable) {
            // Audio hardware unavailable; fail safely without crashing
        }
    }

    fun release() {
        isReleased = true
        try {
            soundPool?.release()
        } catch (_: Throwable) {
            // Ignore release errors
        } finally {
            soundPool = null
        }
    }
}
