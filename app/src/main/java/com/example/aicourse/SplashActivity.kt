package com.example.aicourse

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            super.onCreate(savedInstanceState)

            splashScreen.setOnExitAnimationListener { splashScreenView ->
                // Create an animation for the icon
                val scaleX = ObjectAnimator.ofFloat(
                    splashScreenView.iconView,
                    View.SCALE_X,
                    1f,
                    0f
                )
                val scaleY = ObjectAnimator.ofFloat(
                    splashScreenView.iconView,
                    View.SCALE_Y,
                    1f,
                    0f
                )

                // Customize the animation
                scaleX.interpolator = OvershootInterpolator()
                scaleY.interpolator = OvershootInterpolator()
                scaleX.duration = 500L
                scaleY.duration = 500L

                // Start the animations
                scaleX.start()
                scaleY.start()

                // When animation ends, remove splash and start MainActivity
                scaleX.doOnEnd {
                    splashScreenView.remove()
                    startMainActivity()
                }
            }
        } else {
            // For older Android versions
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_splash_legacy)
            
            // Use a handler to delay the transition
            Handler(Looper.getMainLooper()).postDelayed({
                startMainActivity()
            }, 1000) // 1 second delay
        }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
