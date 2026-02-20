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
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    
    private lateinit var audioManager: AudioManager
    private lateinit var mediaSessionManager: MediaSessionManager
    
    // Configuration
    private var isEnabled = true
    private var isDynamicMode = true
    private var pauseThresholdKmh = 0.5
    private var minVolumePercent = 30
    private var maxVolumePercent = 100
    private var speedForMaxVolume = 30.0
    private var defaultVolumePercent = 70
    
    // State
    private var currentSpeedKmh = 0.0
    private var isMusicPlaying = false
    private var wasMusicPlayingBeforeStop = false
    private var speedJob: Job? = null
    
    // Callbacks for UI updates
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
        
        val currentVolume = getCurrentVolumePercent()
        defaultVolumePercent = currentVolume
        
        // Start simulating speed data for testing (replace with Karoo Extension data later)
        startSpeedSimulation()
    }
    
    private fun startSpeedSimulation() {
        speedJob = serviceScope.launch {
            while (isActive) {
                // TODO: Replace with actual Karoo Extension speed data
                // For now, just use a test value
                delay(1000)
            }
        }
    }
    
    fun updateSpeed(speedKmh: Double) {
        currentSpeedKmh = speedKmh
        onSpeedUpdate?.invoke(speedKmh)
        handleSpeedChange(speedKmh)
    }
    
    private fun handleSpeedChange(speedKmh: Double) {
        if (!isEnabled) return
        
        Log.d(TAG, "Speed: $speedKmh km/h, Dynamic Mode: $isDynamicMode")
        
        if (!isDynamicMode) {
            return
        }
        
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
        val targetVolume = (maxVolume * percent / 100).coerceIn(0, maxVolume)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        Log.d(TAG, "Volume set to $percent% (level $targetVolume/$maxVolume)")
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
        Log.d(TAG, "Service ${if (enabled) "enabled" else "disabled"}")
        onStatusUpdate?.invoke()
    }
    
    fun setDynamicMode(enabled: Boolean) {
        isDynamicMode = enabled
        if (enabled) {
            Log.d(TAG, "Switched to DYNAMIC mode")
        } else {
            Log.d(TAG, "Switched to NORMAL mode")
            setVolumePercent(defaultVolumePercent)
        }
        onStatusUpdate?.invoke()
    }
    
    fun setPauseThreshold(kmh: Double) {
        pauseThresholdKmh = kmh.coerceAtLeast(0.0)
        Log.d(TAG, "Pause threshold set to $pauseThresholdKmh km/h")
    }
    
    fun setDefaultVolume(percent: Int) {
        defaultVolumePercent = percent.coerceIn(0, 100)
        if (!isDynamicMode) {
            setVolumePercent(defaultVolumePercent)
        }
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
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        speedJob?.cancel()
        Log.d(TAG, "Service destroyed")
    }
}