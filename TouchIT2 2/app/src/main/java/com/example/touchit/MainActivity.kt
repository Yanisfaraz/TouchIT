package com.example.touchit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var gameFrame: FrameLayout
    private lateinit var gameManager: IGameManager
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var livesTextView: TextView
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var backgroundImageView: ImageView
    private lateinit var heartsContainer: View
    private lateinit var welcomeManager: IWelcomeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hideSystemUI()
        initializeViews()
        configureViews()

        val gameOverLayout = layoutInflater.inflate(R.layout.game_over_layout, null)
        val welcomeLayout = layoutInflater.inflate(R.layout.welcome_layout, null)

        gameFrame.addView(gameOverLayout)
        gameFrame.addView(welcomeLayout)

        setupManagers(gameOverLayout, welcomeLayout)
        welcomeManager.showWelcome()
    }

    private fun initializeViews() {
        gameFrame = findViewById(R.id.gameFrame)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)
        livesTextView = findViewById(R.id.livesTextView)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        backgroundImageView = findViewById(R.id.backgroundImageView)
        heartsContainer = findViewById(R.id.heartsContainer)
    }

    private fun configureViews() {
        scoreTextView.setTextColor(Color.WHITE)
        timerTextView.setTextColor(Color.WHITE)
        livesTextView.setTextColor(Color.WHITE)

        backgroundImageView.apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 0.5f
        }

        listOf(heart1, heart2, heart3).forEach { heart ->
            heart.apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = null
            }
        }
    }

    private fun setupManagers(gameOverLayout: View, welcomeLayout: View) {
        val handler = Handler(Looper.getMainLooper())

        val uiManager = UIManager(
            activity = this,
            scoreTextView = scoreTextView,
            timerTextView = timerTextView,
            livesTextView = livesTextView,
            heart1 = heart1,
            heart2 = heart2,
            heart3 = heart3
        )

        val targetManager = TargetManager(
            activity = this,
            gameFrame = gameFrame,
            handler = handler
        )

        val gameOverManager = GameOverManager(
            activity = this,
            gameFrame = gameFrame,
            gameOverLayout = gameOverLayout,
            targetManager = targetManager
        )

        welcomeManager = WelcomeManager(
            activity = this,
            gameFrame = gameFrame,
            welcomeLayout = welcomeLayout
        )

        gameManager = GameManager(
            activity = this,
            uiManager = uiManager,
            targetManager = targetManager,
            gameOverManager = gameOverManager
        )

        welcomeManager.setOnStartListener {
            gameManager.startNewGame()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    override fun onPause() {
        super.onPause()
        gameManager.stopGame()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameManager.cleanup()
    }
}