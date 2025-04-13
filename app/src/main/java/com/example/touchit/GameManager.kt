package com.example.touchit

import android.app.Activity

class GameManager(
    private val activity: Activity,
    private val uiManager: IUIManager,
    private val targetManager: ITargetManager,
    private val gameOverManager: IGameOverManager
) : IGameManager {

    private var score = 0
    private var lives = 3
    private var highScore = 0
    private var isGameRunning = false

    init {
        targetManager.setOnTargetHitListener { view ->
            if (isGameRunning && view is Target) {
                score += view.getPoints()
                if (view.type == TargetType.BLACK_TRAP) {
                    uiManager.reduceTime(2)
                }
                uiManager.updateUI(score, uiManager.getCurrentTime(), lives)
            }
        }

        targetManager.setOnTargetMissedListener {
            if (isGameRunning) {
                lives--
                uiManager.blinkLife()
                uiManager.updateUI(score, uiManager.getCurrentTime(), lives)

                if (lives <= 0) {
                    endGame()
                }
            }
        }

        uiManager.setOnTimeUpListener {
            if (isGameRunning) {
                endGame()
            }
        }

        gameOverManager.setOnRestartListener {
            startNewGame()
        }
    }

    override fun startNewGame() {
        isGameRunning = true
        score = 0
        lives = 3
        uiManager.updateUI(score, 30, lives)
        uiManager.startTimer()
        targetManager.startSpawning()
    }

    private fun endGame() {
        isGameRunning = false
        targetManager.stopSpawning()
        uiManager.stopTimer()

        if (score > highScore) {
            highScore = score
        }

        gameOverManager.showGameOver(score, highScore)
    }

    override fun stopGame() {
        isGameRunning = false
        targetManager.stopSpawning()
        uiManager.stopTimer()
    }

    override fun cleanup() {
        targetManager.cleanup()
        uiManager.cleanup()
        gameOverManager.cleanup()
    }
}