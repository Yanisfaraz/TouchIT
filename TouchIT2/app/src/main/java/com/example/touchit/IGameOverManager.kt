package com.example.touchit

interface IGameOverManager {
    fun showGameOver(finalScore: Int, highScore: Int)
    fun hideGameOver()
    fun setOnRestartListener(listener: () -> Unit)
    fun setOnQuitListener(listener: () -> Unit) // Gardé pour compatibilité
    fun cleanup()
}