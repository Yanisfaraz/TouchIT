package com.example.touchit

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.ImageView
import android.graphics.Color
import java.util.Timer
import java.util.TimerTask

class UIManager(
    private val activity: Activity,
    private val scoreTextView: TextView,
    private val timerTextView: TextView,
    private val livesTextView: TextView,
    private val heart1: ImageView,
    private val heart2: ImageView,
    private val heart3: ImageView
) : IUIManager {

    private var timer: Timer? = null
    private var currentTime: Int = 30
    private var onTimeUpListener: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())
    private val hearts = listOf(heart3, heart2, heart1)  // Ordre inversé pour la suppression de droite à gauche

    init {
        scoreTextView.setTextColor(Color.WHITE)
        timerTextView.setTextColor(Color.WHITE)
        livesTextView.setTextColor(Color.WHITE)
        updateHearts(3)
    }

    override fun updateUI(score: Int, timeLeft: Int, lives: Int) {
        handler.post {
            scoreTextView.text = "Score: $score"
            timerTextView.text = "Time: $currentTime"
            livesTextView.text = "Lives: $lives"
            updateHearts(lives)

            if (currentTime <= 0) {
                onTimeUpListener?.invoke()
                stopTimer()
            }
        }
    }

    private fun updateHearts(lives: Int) {
        hearts.forEachIndexed { index, heart ->
            heart.visibility = if (index < lives) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun startTimer() {
        stopTimer()
        currentTime = 60
        timer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    handler.post {
                        currentTime--
                        val currentScore = scoreTextView.text.toString().split(" ")[1].toInt()
                        val currentLives = livesTextView.text.toString().split(" ")[1].toInt()
                        updateUI(currentScore, currentTime, currentLives)
                    }
                }
            }, 1000, 1000)
        }
    }

    override fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    override fun blinkLife() {
        handler.post {
            val lastVisibleHeartIndex = hearts.indexOfFirst { it.visibility == View.VISIBLE }
            if (lastVisibleHeartIndex >= 0) {
                val heart = hearts[lastVisibleHeartIndex]
                heart.alpha = 0f
                heart.postDelayed({
                    heart.alpha = 1f
                    heart.postDelayed({
                        heart.visibility = View.INVISIBLE
                    }, 200)
                }, 200)
            }
        }
    }

    override fun setOnTimeUpListener(listener: () -> Unit) {
        onTimeUpListener = listener
    }

    override fun cleanup() {
        stopTimer()
        onTimeUpListener = null
    }

    override fun getCurrentTime(): Int = currentTime

    override fun reduceTime(seconds: Int) {
        currentTime = (currentTime - seconds).coerceAtLeast(0)
        val currentScore = scoreTextView.text.toString().split(" ")[1].toInt()
        val currentLives = livesTextView.text.toString().split(" ")[1].toInt()
        updateUI(currentScore, currentTime, currentLives)
    }
}