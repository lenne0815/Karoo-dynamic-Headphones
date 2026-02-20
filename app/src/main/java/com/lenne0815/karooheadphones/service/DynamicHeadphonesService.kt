package com.lenne0815.karooheadphones.service

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

class DynamicHeadphonesService : Service() {
    
    private val TAG = "DynamicHeadphones"
    private val binder = LocalBinder()
    
    private lateinit var audioManager: AudioManager
    private lateinit var mediaSessionManager: MediaSessionManager
    
    private var isEnabled = true
    private var isDynamicMode = true
    private var pauseThresholdKmh = 0.5
    private var minVolumePercent = 30
    private var maxVolumePercent = 100
    private var speedForMaxVolume = 30.0
    private var defaultVolumePercent = 70
    
    private var currentSpeedKmh = 0.0
    private var isMusicPlaying = false
    private var wasMusicPlayingBeforeStop = false
    
    var onSpeedUpdate: ((Double) -> Unit)? = null
    var onStatusUpdate: (() -> Unit)? = null
    
    inner class LocalBinder : Binder() {
        fun getService(): DynamicHeadphonesService = this@DynamicHeadphonesService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        mediaSessionManager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
        defaultVolumePercent = getCurrentVolumePercent()
    }
    
    fun updateSpeed(speedKmh: Double) {
        currentSpeedKmh = speedKmh
        onSpeedUpdate?.invoke(speedKmh)
        if (isEnabled && isDynamicMode) {
            handleSpeedChange(speedKmh)
        }
    }
    
    private fun handleSpeedChange(speedKmh: Double) {
        if (speedKmh < pauseThresholdKmh) {
            if (isMusicPlaying) {
                wasMusicPlayingBeforeStop = true
                pauseMusic()
            }
            setVolumePercent(minVolumePercent)
        } else {
            if (wasMusicPlayingBeforeStop && !isMusicPlaying) {
                playMusic()
            }
            
            val volumePercent = calculateVolumeForSpeed(speedKmh)
            setVolumePercent(volumePercent)
        }
        
        onStatusUpdate?.invoke()
    }
    
    private fun calculateVolumeForSpeed(speedKmh: Double): Int {
        return when {
            speedKmh <= pauseThresholdKmh -> minVolumePercent
            speedKmh >= speedForMaxVolume -> maxVolumePercent
            else -> {
                val ratio = (speedKmh - pauseThresholdKmh) / (speedForMaxVolume - pauseThresholdKmh)
                (minVolumePercent + (maxVolumePercent - minVolumePercent) * ratio).toInt()
            }
        }
    }
    
    private fun getCurrentVolumePercent(): Int {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        return if (maxVolume > 0) (currentVolume * 100 / maxVolume) else 50
    }
    
    private fun setVolumePercent(percent: Int) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val targetVolume = ((maxVolume * percent) / 100).coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        Log.d(TAG, "Volume: $percent%")
    }
    
    private fun pauseMusic() {
        val controllers = mediaSessionManager.getActiveSessions(null)
        for (controller in controllers) {
            controller.transportControls?.pause()
        }
        isMusicPlaying = false
        Log.d(TAG, "Music paused")
    }
    
    private fun playMusic() {
        val controllers = mediaSessionManager.getActiveSessions(null)
        for (controller in controllers) {
            controller.transportControls?.play()
        }
        isMusicPlaying = true
        wasMusicPlayingBeforeStop = false
        Log.d(TAG, "Music resumed")
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        onStatusUpdate?.invoke()
    }
    
    fun setDynamicMode(enabled: Boolean) {
        isDynamicMode = enabled
        if (!enabled) setVolumePercent(defaultVolumePercent)
        onStatusUpdate?.invoke()
    }
    
    fun setPauseThreshold(kmh: Double) { pauseThresholdKmh = kmh.coerceAtLeast(0.0) }
    fun setDefaultVolume(percent: Int) { 
        defaultVolumePercent = percent.coerceIn(0, 100)
        if (!isDynamicMode) setVolumePercent(defaultVolumePercent)
    }
    fun setVolumeRange(minPercent: Int, maxPercent: Int) {
        minVolumePercent = minPercent.coerceIn(0, 100)
        maxVolumePercent = maxPercent.coerceIn(0, 100)
    }
    
    fun getCurrentSpeed(): Double = currentSpeedKmh
    fun isServiceEnabled(): Boolean = isEnabled
    fun isDynamicModeEnabled(): Boolean = isDynamicMode
    fun getPauseThreshold(): Double = pauseThresholdKmh
    fun getDefaultVolume(): Int = defaultVolumePercent
    fun getMinVolume(): Int = minVolumePercent
    fun getMaxVolume(): Int = maxVolumePercent
    
    override fun onBind(intent: Intent): IBinder = binder
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
}