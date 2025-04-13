package com.example.touchit

interface IWelcomeManager {
    fun showWelcome()
    fun hideWelcome()
    fun setOnStartListener(listener: () -> Unit)
    fun cleanup()
}