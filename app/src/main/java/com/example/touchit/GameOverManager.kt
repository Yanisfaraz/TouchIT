package com.example.touchit

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

class GameOverManager(
    private val activity: Activity,
    private val gameFrame: FrameLayout,
    private val gameOverLayout: View,
    private val targetManager: ITargetManager // Ajout du TargetManager
) : IGameOverManager {

    private var onRestartListener: (() -> Unit)? = null
    private var onQuitListener: (() -> Unit)? = null
    private val finalScoreTextView: TextView = gameOverLayout.findViewById(R.id.finalScoreTextView)
    private val highScoreTextView: TextView = gameOverLayout.findViewById(R.id.highScoreTextView)

    init {
        val gameOverImage = gameOverLayout.findViewById<ImageView>(R.id.gameOverImage)
        gameOverImage.setOnClickListener {
            onRestartListener?.invoke()
            hideGameOver()
        }
    }

    override fun showGameOver(finalScore: Int, highScore: Int) {
        activity.runOnUiThread {
            // Arrête et nettoie les cibles
            targetManager.stopSpawning()
            targetManager.cleanup()

            finalScoreTextView.text = "Score: $finalScore"
            highScoreTextView.text = "Meilleur score: $highScore"
            gameOverLayout.visibility = View.VISIBLE
            gameOverLayout.bringToFront()
        }
    }

    override fun hideGameOver() {
        activity.runOnUiThread {
            gameOverLayout.visibility = View.GONE
        }
    }

    override fun setOnRestartListener(listener: () -> Unit) {
        onRestartListener = listener
    }

    override fun setOnQuitListener(listener: () -> Unit) {
        onQuitListener = listener
    }

    override fun cleanup() {
        onRestartListener = null
        onQuitListener = null
    }
}