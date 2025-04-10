package com.example.touchit

import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Animation du texte "Tap the target to start"
        val tapTargetText = findViewById<TextView>(R.id.tapTargetText)

        // Animation de clignotement pour le texte "Tap the target to start"
        val blinkAnimation = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 1000
            repeatMode = AlphaAnimation.REVERSE
            repeatCount = AlphaAnimation.INFINITE
        }
        tapTargetText.startAnimation(blinkAnimation)

        // Animation de pulsation pour le logo
        val startTarget = findViewById<ImageView>(R.id.startTarget)
        val pulseAnimation = ScaleAnimation(
            1f, 1.2f,
            1f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1000
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        startTarget.startAnimation(pulseAnimation)

        // Gestionnaire de clic sur la cible
        startTarget.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}