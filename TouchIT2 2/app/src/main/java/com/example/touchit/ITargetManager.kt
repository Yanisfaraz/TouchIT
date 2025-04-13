package com.example.touchit

import android.view.View

interface ITargetManager {
    fun startSpawning()
    fun stopSpawning()
    fun cleanup()
    fun setOnTargetHitListener(listener: (View) -> Unit)
    fun setOnTargetMissedListener(listener: () -> Unit)
}