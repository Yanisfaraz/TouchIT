package com.example.touchit

interface IUIManager {
    fun updateUI(score: Int, timeLeft: Int, lives: Int)
    fun startTimer()
    fun stopTimer()
    fun blinkLife()
    fun setOnTimeUpListener(listener: () -> Unit)
    fun cleanup()
    fun getCurrentTime(): Int
    fun reduceTime(seconds: Int)
}