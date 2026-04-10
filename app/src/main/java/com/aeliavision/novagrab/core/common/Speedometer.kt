package com.aeliavision.novagrab.core.common

import android.os.SystemClock


/**
 * Utility to calculate download speed in bytes per second.
 * Uses a simple EMA (Exponential Moving Average) to smooth out fluctuations.
 */
class Speedometer {
    private var lastTimeMs: Long = SystemClock.elapsedRealtime()
    private var lastBytes: Long = 0L
    private var currentSpeed: Double = 0.0
    private val alpha = 0.3 // Smoothing factor (0 < alpha < 1)

    @Synchronized
    fun update(totalDownloadedBytes: Long): Long {
        val now = SystemClock.elapsedRealtime()
        val dtMs = now - lastTimeMs
        
        // Only update if at least 100ms has passed to avoid noise
        if (dtMs < 100) return currentSpeed.toLong()

        val deltaBytes = totalDownloadedBytes - lastBytes
        if (deltaBytes < 0) {
            // Reset if total bytes decreased (should not happen in normal download)
            lastBytes = totalDownloadedBytes
            lastTimeMs = now
            return currentSpeed.toLong()
        }

        val instantSpeed = (deltaBytes * 1000.0) / dtMs
        
        // Apply EMA: current = alpha * instant + (1 - alpha) * previous
        currentSpeed = if (currentSpeed == 0.0) {
            instantSpeed
        } else {
            alpha * instantSpeed + (1 - alpha) * currentSpeed
        }

        lastBytes = totalDownloadedBytes
        lastTimeMs = now
        
        return currentSpeed.toLong()
    }
}
