package com.example.touchit

import android.app.Activity
import android.view.View
import android.widget.FrameLayout

class WelcomeManager(
    private val activity: Activity,
    private val gameFrame: FrameLayout,
    private val welcomeLayout: View
) : IWelcomeManager {

    private var onStartListener: (() -> Unit)? = null

    init {
        welcomeLayout.setOnClickListener {
            onStartListener?.invoke()
            hideWelcome()
        }
    }

    override fun showWelcome() {
        activity.runOnUiThread {
            welcomeLayout.visibility = View.VISIBLE
            welcomeLayout.bringToFront()
        }
    }

    override fun hideWelcome() {
        activity.runOnUiThread {
            welcomeLayout.visibility = View.GONE
        }
    }

    override fun setOnStartListener(listener: () -> Unit) {
        onStartListener = listener
    }

    override fun cleanup() {
        onStartListener = null
    }
}