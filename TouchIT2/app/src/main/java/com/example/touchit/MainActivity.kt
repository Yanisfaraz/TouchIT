package com.example.touchit

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var gameFrame: FrameLayout
    private lateinit var hearts: List<ImageView>
    private lateinit var gameOverLayout: View
    private lateinit var finalScoreText: TextView
    private lateinit var highScoreText: TextView
    private lateinit var playAgainText: TextView
    private var blinkAnimationRunnable: Runnable? = null
    private var lives = 3
    private var score = 0
    private var timeLeft = 60
    private var gameActive = true
    private val handler = Handler(Looper.getMainLooper())
    private var currentTarget: View? = null
    private var currentAnimationRunnable: Runnable? = null
    private var currentVelocityX = 0f
    private var currentVelocityY = 0f
    private val SPEED = 10f
    private var gameStarted = false
    private var timer: CountDownTimer? = null
    private var purpleTargetsCount = 0
    private var targets = mutableListOf<View>()
    private var velocities = mutableMapOf<View, Pair<Float, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            scoreTextView = findViewById(R.id.scoreTextView)
            timerTextView = findViewById(R.id.timerTextView)
            gameFrame = findViewById(R.id.gameFrame)
            hearts = listOf(
                findViewById(R.id.heart1),
                findViewById(R.id.heart2),
                findViewById(R.id.heart3)
            )

            gameOverLayout = layoutInflater.inflate(R.layout.game_over_layout, null)
            finalScoreText = gameOverLayout.findViewById(R.id.finalScoreText)
            highScoreText = gameOverLayout.findViewById(R.id.highScoreText)
            playAgainText = gameOverLayout.findViewById(R.id.playAgainText)

            playAgainText.setOnClickListener {
                if (gameOverLayout.parent != null) {
                    val gameOverImage = gameOverLayout.findViewById<ImageView>(R.id.gameOverImage)
                    gameOverImage.clearAnimation()
                    gameFrame.removeView(gameOverLayout)
                }
                startGame()
            }

            scoreTextView.text = "Score: 0"
            timerTextView.text = "Time: 60"

            handler.postDelayed({
                if (!gameStarted) {
                    gameStarted = true
                    startGame()
                }
            }, 500)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur lors du démarrage: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun updateHighScore() {
        val sharedPref = getSharedPreferences("game_data", MODE_PRIVATE)
        val currentHighScore = sharedPref.getInt("high_score", 0)

        if (score > currentHighScore) {
            with(sharedPref.edit()) {
                putInt("high_score", score)
                apply()
            }
        }
    }

    private fun getHighScore(): Int {
        val sharedPref = getSharedPreferences("game_data", MODE_PRIVATE)
        return sharedPref.getInt("high_score", 0)
    }

    private fun showPointsAnimation(x: Int, y: Int, points: Int) {
        val pointsText = TextView(this).apply {
            text = if (points >= 0) "+$points" else "$points"
            textSize = 20f
            setTextColor(
                when {
                    points < 0 -> Color.RED
                    points > 0 -> Color.GREEN
                    else -> Color.WHITE
                }
            )
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = x
                topMargin = y - 50
            }
        }

        gameFrame.addView(pointsText)

        pointsText.animate()
            .translationY(-100f)
            .alpha(0f)
            .setDuration(1000)
            .withEndAction {
                gameFrame.removeView(pointsText)
            }
            .start()
    }

    private fun updateHearts() {
        hearts.forEachIndexed { index, heart ->
            heart.visibility = if (index < lives) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun loseLife() {
        lives--
        updateHearts()

        // Animation de rougissement uniquement
        val redOverlay = View(this).apply {
            setBackgroundColor(Color.RED)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            alpha = 0f
        }

        gameFrame.addView(redOverlay)

        redOverlay.animate()
            .alpha(0.5f)
            .setDuration(250)
            .withEndAction {
                redOverlay.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction {
                        gameFrame.removeView(redOverlay)
                    }
                    .start()
            }
            .start()

        if (lives <= 0) {
            showGameOver()
        }
    }

    private fun startGame() {
        try {
            gameOverLayout.findViewById<ImageView>(R.id.gameOverImage)?.clearAnimation()
            blinkAnimationRunnable?.let { handler.removeCallbacks(it) }
            blinkAnimationRunnable = null

            score = 0
            timeLeft = 60
            gameActive = true
            lives = 3
            purpleTargetsCount = 0
            targets.clear()
            velocities.clear()
            updateHearts()
            scoreTextView.text = "Score: $score"
            timerTextView.text = "Time: $timeLeft"

            timer?.cancel()
            timer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = (millisUntilFinished / 1000).toInt()
                    timerTextView.text = "Time: $timeLeft"
                }

                override fun onFinish() {
                    showGameOver()
                }
            }.start()

            handler.postDelayed({ generateNewTarget() }, 1000)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur dans startGame: ${e.message}")
        }
    }

    private fun showGameOver() {
        gameActive = false
        timer?.cancel()

        updateHighScore()
        val highScore = getHighScore()

        finalScoreText.text = "Score final: $score"
        highScoreText.text = "Meilleur score: $highScore"

        if (score >= highScore) {
            highScoreText.setTextColor(Color.GREEN)
        } else {
            highScoreText.setTextColor(Color.parseColor("#FFD700"))
        }

        targets.forEach { gameFrame.removeView(it) }
        targets.clear()
        velocities.clear()

        blinkAnimationRunnable?.let { handler.removeCallbacks(it) }

        val gameOverImage = gameOverLayout.findViewById<ImageView>(R.id.gameOverImage)

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

        gameOverImage.startAnimation(pulseAnimation)

        if (gameOverLayout.parent != null) {
            (gameOverLayout.parent as ViewGroup).removeView(gameOverLayout)
        }

        gameFrame.addView(gameOverLayout)
        playAgainText.visibility = View.VISIBLE

        blinkAnimationRunnable = object : Runnable {
            override fun run() {
                if (gameActive) {
                    handler.removeCallbacks(this)
                    return
                }
                playAgainText.visibility = if (playAgainText.visibility == View.VISIBLE) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }
                handler.postDelayed(this, 500)
            }
        }
        handler.post(blinkAnimationRunnable!!)
    }

    private fun generateNewTarget(isDuplicate: Boolean = false, position: Pair<Int, Int>? = null) {
        try {
            if (!gameActive) return

            currentAnimationRunnable?.let {
                handler.removeCallbacks(it)
                currentAnimationRunnable = null
            }

            if (!isDuplicate) {
                targets.forEach { gameFrame.removeView(it) }
                targets.clear()
                velocities.clear()
                purpleTargetsCount = 0
                currentTarget = null
            }

            val targetType = if (isDuplicate) {
                "purple_duplicate"
            } else {
                if (purpleTargetsCount > 0) {
                    listOf("red", "blue", "black").random()
                } else {
                    listOf("red", "blue", "black", "purple").random()
                }
            }

            val targetPoints = mapOf(
                "red" to 4,
                "blue" to 2,
                "black" to -2,
                "purple" to 1,
                "purple_duplicate" to 3
            )

            val standardSize = 150
            val blackTargetSize = 250

            val currentTargetSize = if (targetType == "black") blackTargetSize else standardSize
            val maxWidth = gameFrame.width - currentTargetSize
            val maxHeight = gameFrame.height - currentTargetSize

            if (maxWidth <= 0 || maxHeight <= 0) {
                handler.postDelayed({ generateNewTarget(isDuplicate, position) }, 100)
                return
            }

            val targetView = if (targetType == "black") {
                ImageView(this).apply {
                    setImageResource(R.drawable.trap)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutParams = FrameLayout.LayoutParams(blackTargetSize, blackTargetSize)
                }
            } else {
                View(this).apply {
                    setBackgroundColor(
                        when (targetType) {
                            "red" -> Color.RED
                            "blue" -> Color.BLUE
                            "purple", "purple_duplicate" -> Color.MAGENTA
                            else -> Color.WHITE
                        }
                    )
                    layoutParams = FrameLayout.LayoutParams(standardSize, standardSize)
                }
            }.apply {
                (layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = position?.first ?: Random.nextInt(0, maxWidth)
                    topMargin = position?.second ?: Random.nextInt(0, maxHeight)
                }
                tag = targetType
                setOnClickListener { view ->
                    if (!gameActive) return@setOnClickListener

                    val points = targetPoints[targetType] ?: 0
                    score += points
                    scoreTextView.text = "Score: $score"

                    val clickX = (view.layoutParams as FrameLayout.LayoutParams).leftMargin
                    val clickY = (view.layoutParams as FrameLayout.LayoutParams).topMargin
                    showPointsAnimation(clickX, clickY, points)

                    val clickAnimation = ScaleAnimation(
                        1f, 0f,
                        1f, 0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = 200
                        setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationRepeat(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                when (targetType) {
                                    "black" -> {
                                        timeLeft = (timeLeft - 2).coerceAtLeast(0)
                                        timerTextView.text = "Time: $timeLeft"
                                        timer?.cancel()
                                        timer = object : CountDownTimer(timeLeft * 1000L, 1000) {
                                            override fun onTick(millisUntilFinished: Long) {
                                                timeLeft = (millisUntilFinished / 1000).toInt()
                                                timerTextView.text = "Time: $timeLeft"
                                            }
                                            override fun onFinish() {
                                                showGameOver()
                                            }
                                        }.start()
                                        gameFrame.removeView(view)
                                        targets.remove(view)
                                        velocities.remove(view)
                                        generateNewTarget()
                                    }
                                    "purple" -> {
                                        try {
                                            gameFrame.removeView(view)
                                            targets.remove(view)
                                            velocities.remove(view)

                                            generateNewTarget(true, Pair(clickX - 75, clickY))
                                            generateNewTarget(true, Pair(clickX + 75, clickY))
                                            purpleTargetsCount = 2
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Erreur cible mauve: ${e.message}")
                                            generateNewTarget()
                                        }
                                    }
                                    "purple_duplicate" -> {
                                        try {
                                            gameFrame.removeView(view)
                                            targets.remove(view)
                                            velocities.remove(view)
                                            purpleTargetsCount--

                                            if (targets.isEmpty()) {
                                                generateNewTarget()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Erreur cible mauve dupliquée: ${e.message}")
                                            generateNewTarget()
                                        }
                                    }
                                    else -> {
                                        gameFrame.removeView(view)
                                        targets.remove(view)
                                        velocities.remove(view)
                                        generateNewTarget()
                                    }
                                }
                            }
                        })
                    }
                    view.startAnimation(clickAnimation)
                }
            }

            if (targetType == "purple") {
                purpleTargetsCount++
            }

            targets.add(targetView)
            gameFrame.addView(targetView)

            if (targetType == "red" || targetType == "purple_duplicate") {
                val angle = if (targetType == "purple_duplicate") {
                    Random.nextDouble(-Math.PI/4, Math.PI/4)
                } else {
                    Random.nextDouble(0.0, 2 * Math.PI)
                }
                val velocityX = (SPEED * cos(angle)).toFloat()
                val velocityY = (SPEED * sin(angle)).toFloat()
                velocities[targetView] = Pair(velocityX, velocityY)
                animateTarget(targetView, maxWidth, maxHeight)
            }

            val displayTime = when (targetType) {
                "red" -> 1500L
                "purple" -> 2000L
                "purple_duplicate" -> 2000L
                else -> 800L
            }

            if (targetType != "purple_duplicate") {
                handler.postDelayed({
                    if (gameActive && targets.contains(targetView)) {
                        gameFrame.removeView(targetView)
                        targets.remove(targetView)
                        velocities.remove(targetView)
                        if (targetType != "black") {
                            loseLife()
                        }
                        if (gameActive) {
                            if (targetType == "purple") {
                                purpleTargetsCount = 0
                            }
                            generateNewTarget()
                        }
                    }
                }, displayTime)
            }

        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur dans generateNewTarget: ${e.message}")
        }
    }

    private fun animateTarget(targetView: View, maxWidth: Int, maxHeight: Int) {
        try {
            val initialVelocity = velocities[targetView] ?: return
            val runnable = object : Runnable {
                override fun run() {
                    try {
                        if (!gameActive || !targets.contains(targetView)) return

                        val params = targetView.layoutParams as FrameLayout.LayoutParams
                        val currentVelocity = velocities[targetView] ?: initialVelocity
                        var velocityX = currentVelocity.first
                        var velocityY = currentVelocity.second

                        var newX = params.leftMargin.toFloat() + velocityX
                        var newY = params.topMargin.toFloat() + velocityY

                        if (newX <= 0f) {
                            newX = 0f
                            velocityX = -velocityX
                        } else if (newX >= maxWidth.toFloat()) {
                            newX = maxWidth.toFloat()
                            velocityX = -velocityX
                        }

                        if (newY <= 0f) {
                            newY = 0f
                            velocityY = -velocityY
                        } else if (newY >= maxHeight.toFloat()) {
                            newY = maxHeight.toFloat()
                            velocityY = -velocityY
                        }

                        params.leftMargin = newX.toInt()
                        params.topMargin = newY.toInt()
                        targetView.layoutParams = params

                        velocities[targetView] = Pair(velocityX, velocityY)

                        if (gameActive && targets.contains(targetView)) {
                            handler.postDelayed(this, 16)
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Erreur dans animation: ${e.message}")
                    }
                }
            }
            handler.post(runnable)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur dans animateTarget: ${e.message}")
        }
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            timer?.cancel()
            handler.removeCallbacksAndMessages(null)
            blinkAnimationRunnable?.let { handler.removeCallbacks(it) }
            gameOverLayout.findViewById<ImageView>(R.id.gameOverImage)?.clearAnimation()
            gameActive = false
            targets.clear()
            velocities.clear()
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur dans onDestroy: ${e.message}")
        }
    }
}