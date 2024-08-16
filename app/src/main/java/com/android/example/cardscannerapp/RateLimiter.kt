package com.android.example.cardscannerapp

class RateLimiter(private val intervalMillis: Long) {
    private var lastTimeTriggered: Long = 0

    fun shouldProceed(): Boolean {
        val currentTime = System.currentTimeMillis()
        return if (currentTime - lastTimeTriggered > intervalMillis) {
            lastTimeTriggered = currentTime
            true
        } else {
            false
        }
    }
}