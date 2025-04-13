package com.example.touchit

import android.graphics.Color

enum class TargetType {
    RED_MOVING,      // Cible rouge mobile (4 points)
    PURPLE_STATIC,   // Cible violette statique qui se divise (1 point)
    PURPLE_MOVING,   // Cible violette mobile après division (3 points)
    BLUE_STATIC,     // Cible bleue statique (2 points)
    BLACK_TRAP       // Piège noir (-4 points, -2 secondes)
}

object TargetConfigs {
    val POINTS = mapOf(
        TargetType.RED_MOVING to 4,      // Cible plus difficile à toucher
        TargetType.PURPLE_STATIC to 1,   // Cible basique qui se divise
        TargetType.PURPLE_MOVING to 3,   // Cibles issues de la division
        TargetType.BLUE_STATIC to 2,     // Cible basique
        TargetType.BLACK_TRAP to -4      // Pénalité si touchée
    )

    val COLORS = mapOf(
        TargetType.RED_MOVING to Color.RED,
        TargetType.PURPLE_STATIC to Color.MAGENTA,
        TargetType.PURPLE_MOVING to Color.MAGENTA,
        TargetType.BLUE_STATIC to Color.BLUE,
        TargetType.BLACK_TRAP to Color.BLACK
    )

    val SPEEDS = mapOf(
        TargetType.RED_MOVING to 5f,      // Vitesse modérée
        TargetType.PURPLE_MOVING to 4f,   // Un peu plus lent que le rouge
        TargetType.PURPLE_STATIC to 0f,   // Statique
        TargetType.BLUE_STATIC to 0f,     // Statique
        TargetType.BLACK_TRAP to 0f       // Statique
    )
}