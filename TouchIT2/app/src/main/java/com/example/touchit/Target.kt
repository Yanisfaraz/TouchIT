package com.example.touchit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlin.math.cos
import kotlin.math.sin

class Target(
    context: Context,
    val type: TargetType,
    private val points: Int,
    private val color: Int,
    private val speed: Float,
    private var direction: Float = 0f
) : View(context) {

    companion object {
        const val TARGET_SIZE = 150
    }

    private val paint = Paint().apply {
        color = this@Target.color
        style = Paint.Style.FILL
    }

    private var onSplitListener: ((Float, Float) -> Unit)? = null
    private var onTimePenaltyListener: (() -> Unit)? = null
    private val trapDrawable: Drawable? = if (type == TargetType.BLACK_TRAP) {
        ContextCompat.getDrawable(context, R.drawable.trap)
    } else {
        null
    }

    init {
        layoutParams = ViewGroup.LayoutParams(TARGET_SIZE, TARGET_SIZE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (type == TargetType.BLACK_TRAP && trapDrawable != null) {
            trapDrawable.setBounds(0, 0, width, height)
            trapDrawable.draw(canvas)
        } else {
            canvas.drawCircle(
                TARGET_SIZE / 2f,
                TARGET_SIZE / 2f,
                TARGET_SIZE / 2f,
                paint
            )
        }
    }

    fun update(maxWidth: Int, maxHeight: Int) {
        if (speed > 0) {
            val dx = speed * cos(Math.toRadians(direction.toDouble())).toFloat()
            val dy = speed * sin(Math.toRadians(direction.toDouble())).toFloat()

            var newX = translationX + dx
            var newY = translationY + dy

            if (newX <= 0 || newX >= maxWidth - TARGET_SIZE) {
                direction = 180 - direction
                newX = translationX
            }
            if (newY <= 0 || newY >= maxHeight - TARGET_SIZE) {
                direction = -direction
                newY = translationY
            }

            translationX = newX
            translationY = newY
        }
    }

    fun handleClick(): Boolean {
        when (type) {
            TargetType.PURPLE_STATIC -> onSplitListener?.invoke(translationX, translationY)
            TargetType.BLACK_TRAP -> onTimePenaltyListener?.invoke()
            else -> {}
        }
        return true
    }

    fun setOnSplitListener(listener: (Float, Float) -> Unit) {
        onSplitListener = listener
    }

    fun setOnTimePenaltyListener(listener: () -> Unit) {
        onTimePenaltyListener = listener
    }

    fun getPoints() = points
}