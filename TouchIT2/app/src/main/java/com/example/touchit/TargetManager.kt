package com.example.touchit

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import java.util.Timer
import java.util.TimerTask

class TargetManager(
    private val activity: Activity,
    private val gameFrame: FrameLayout,
    private val handler: Handler = Handler(Looper.getMainLooper())
) : ITargetManager {

    private var currentTarget: Target? = null
    private var currentTargetTimer: Timer? = null
    private var onTargetHit: ((View) -> Unit)? = null
    private var onTargetMissed: (() -> Unit)? = null
    private val random = kotlin.random.Random(System.currentTimeMillis())
    private val targetFactory = TargetFactory(activity)
    private var isPurpleSpawning = false
    private var purpleTargetsCount = 0

    override fun setOnTargetHitListener(listener: (View) -> Unit) {
        onTargetHit = listener
    }

    override fun setOnTargetMissedListener(listener: () -> Unit) {
        onTargetMissed = listener
    }

    override fun startSpawning() {
        stopSpawning()
        spawnNewTarget()
    }

    override fun stopSpawning() {
        currentTargetTimer?.cancel()
        currentTargetTimer = null
        cleanup()
    }

    override fun cleanup() {
        currentTargetTimer?.cancel()
        currentTargetTimer = null
        currentTarget?.let {
            handler.post {
                gameFrame.removeView(it)
            }
        }
        currentTarget = null
        isPurpleSpawning = false
        purpleTargetsCount = 0
    }

    private fun spawnNewTarget() {
        if (currentTarget != null || (isPurpleSpawning && purpleTargetsCount > 0)) return

        val targetType = if (isPurpleSpawning) {
            TargetType.PURPLE_MOVING
        } else {
            listOf(
                TargetType.RED_MOVING,
                TargetType.PURPLE_STATIC,
                TargetType.BLUE_STATIC,
                TargetType.BLACK_TRAP
            ).random(random)
        }

        val target = targetFactory.createTarget(targetType)
        target.setOnClickListener {
            if (target.handleClick()) {
                onTargetHit?.invoke(target)
                Log.d("TargetManager", "Target clicked: ${target.type} with points: ${target.getPoints()}")
                gameFrame.removeView(target)
                currentTarget = null

                if (targetType == TargetType.PURPLE_STATIC) {
                    isPurpleSpawning = true
                    spawnPurpleMovingTargets(target.translationX, target.translationY)
                } else if (!isPurpleSpawning) {
                    spawnNewTarget()
                }
            }
        }

        val size = Target.TARGET_SIZE
        gameFrame.post {
            val maxX = gameFrame.width - size
            val maxY = gameFrame.height - size

            if (maxX > 0 && maxY > 0) {
                target.translationX = random.nextInt(maxX).toFloat()
                target.translationY = random.nextInt(maxY).toFloat()

                handler.post {
                    gameFrame.addView(target)
                    currentTarget = target

                    currentTargetTimer = Timer()
                    currentTargetTimer?.schedule(object : TimerTask() {
                        override fun run() {
                            handler.post {
                                if (target.parent != null) {
                                    if (target.type != TargetType.BLACK_TRAP) {
                                        onTargetMissed?.invoke()
                                        Log.d("TargetManager", "Target missed!")
                                    }
                                    gameFrame.removeView(target)
                                    currentTarget = null
                                    spawnNewTarget()
                                }
                            }
                        }
                    }, 1500)

                    if (targetType == TargetType.RED_MOVING || targetType == TargetType.PURPLE_MOVING) {
                        startTargetAnimation(target)
                    }
                }
            }
        }
    }

    private fun spawnPurpleMovingTargets(x: Float, y: Float) {
        purpleTargetsCount = 2
        repeat(2) {
            val movingTarget = targetFactory.createTarget(TargetType.PURPLE_MOVING)
            movingTarget.translationX = x
            movingTarget.translationY = y

            movingTarget.setOnClickListener {
                if (movingTarget.handleClick()) {
                    onTargetHit?.invoke(movingTarget)
                    gameFrame.removeView(movingTarget)
                    purpleTargetsCount--
                    if (purpleTargetsCount == 0) {
                        isPurpleSpawning = false
                        spawnNewTarget()
                    }
                }
            }

            handler.post {
                gameFrame.addView(movingTarget)
                startTargetAnimation(movingTarget)
            }

            Timer().schedule(object : TimerTask() {
                override fun run() {
                    handler.post {
                        if (movingTarget.parent != null) {
                            onTargetMissed?.invoke()
                            gameFrame.removeView(movingTarget)
                            purpleTargetsCount--
                            if (purpleTargetsCount == 0) {
                                isPurpleSpawning = false
                                spawnNewTarget()
                            }
                        }
                    }
                }
            }, 3000) // Modifié de 1500 à 3000 millisecondes
        }
    }

    private fun startTargetAnimation(target: Target) {
        handler.post(object : Runnable {
            override fun run() {
                if (target.parent != null) {
                    target.update(gameFrame.width, gameFrame.height)
                    handler.postDelayed(this, 16)
                }
            }
        })
    }
}