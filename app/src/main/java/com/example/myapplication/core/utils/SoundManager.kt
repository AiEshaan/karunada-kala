package com.example.myapplication.core.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.media.SoundPool
import com.example.myapplication.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool
    private val sounds = mutableMapOf<String, Int>()
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
    
    init {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()
        
        // Load default cultural tones if they exist
        try {
            // Note: In a real project, these raw files would be added to res/raw
            // For now, we rely on the ToneGenerator fallback in playSound
            // sounds["success"] = soundPool.load(context, R.raw.cultural_bell, 1)
        } catch (e: Exception) {
            android.util.Log.e("SoundManager", "Failed to load sounds", e)
        }
    }
    
    fun playSound(type: String) {
        val soundId = sounds[type]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            // Robust fallback using ToneGenerator
            when (type.uppercase()) {
                "SUCCESS" -> toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                "TAP" -> toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
                "SHUTTER" -> toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
                else -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            }
        }
    }
    
    fun release() {
        soundPool.release()
        toneGenerator.release()
    }
}
