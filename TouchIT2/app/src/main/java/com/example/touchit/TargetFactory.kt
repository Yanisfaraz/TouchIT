package com.example.touchit

import android.content.Context
import kotlin.random.Random

class TargetFactory(private val context: Context) {
    private val random = Random(System.currentTimeMillis())

    fun createTarget(type: TargetType): Target {
        val target = when (type) {
            TargetType.RED_MOVING -> createRedTarget()
            TargetType.PURPLE_STATIC -> createPurpleStaticTarget()
            TargetType.PURPLE_MOVING -> createPurpleMovingTarget()
            TargetType.BLUE_STATIC -> createBlueTarget()
            TargetType.BLACK_TRAP -> createBlackTrapTarget()
        }

        when (type) {
            TargetType.PURPLE_STATIC -> {
                target.setOnSplitListener { x, y ->
                    // Le listener sera configuré dans TargetManager
                }
            }
            TargetType.BLACK_TRAP -> {
                target.setOnTimePenaltyListener {
                    // Le listener sera configuré dans TargetManager
                }
            }
            else -> {} // Pas de listener spécial pour les autres types
        }

        return target
    }

    private fun createRedTarget() = Target(
        context = context,
        type = TargetType.RED_MOVING,
        points = TargetConfigs.POINTS[TargetType.RED_MOVING]!!,
        color = TargetConfigs.COLORS[TargetType.RED_MOVING]!!,
        speed = TargetConfigs.SPEEDS[TargetType.RED_MOVING]!!,
        direction = random.nextFloat() * 360
    )

    private fun createPurpleStaticTarget() = Target(
        context = context,
        type = TargetType.PURPLE_STATIC,
        points = TargetConfigs.POINTS[TargetType.PURPLE_STATIC]!!,
        color = TargetConfigs.COLORS[TargetType.PURPLE_STATIC]!!,
        speed = TargetConfigs.SPEEDS[TargetType.PURPLE_STATIC]!!
    )

    private fun createPurpleMovingTarget() = Target(
        context = context,
        type = TargetType.PURPLE_MOVING,
        points = TargetConfigs.POINTS[TargetType.PURPLE_MOVING]!!,
        color = TargetConfigs.COLORS[TargetType.PURPLE_MOVING]!!,
        speed = TargetConfigs.SPEEDS[TargetType.PURPLE_MOVING]!!,
        direction = random.nextFloat() * 360
    )

    private fun createBlueTarget() = Target(
        context = context,
        type = TargetType.BLUE_STATIC,
        points = TargetConfigs.POINTS[TargetType.BLUE_STATIC]!!,
        color = TargetConfigs.COLORS[TargetType.BLUE_STATIC]!!,
        speed = TargetConfigs.SPEEDS[TargetType.BLUE_STATIC]!!
    )

    private fun createBlackTrapTarget() = Target(
        context = context,
        type = TargetType.BLACK_TRAP,
        points = TargetConfigs.POINTS[TargetType.BLACK_TRAP]!!,
        color = TargetConfigs.COLORS[TargetType.BLACK_TRAP]!!,
        speed = TargetConfigs.SPEEDS[TargetType.BLACK_TRAP]!!
    )
}