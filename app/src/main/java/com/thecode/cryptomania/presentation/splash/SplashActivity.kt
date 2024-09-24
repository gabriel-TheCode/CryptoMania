package com.thecode.cryptomania.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.thecode.cryptomania.R
import com.thecode.cryptomania.databinding.ActivitySplashBinding
import com.thecode.cryptomania.presentation.main.MainActivity
import com.thecode.cryptomania.presentation.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpObserver()
        setUpAnimation()
    }

    private fun setUpAnimation() {
        binding.layoutImageContent.animation = AnimationUtils.loadAnimation(this, R.anim.up_to_down)
        binding.layoutText.animation = AnimationUtils.loadAnimation(this, R.anim.down_to_up)

        val rotate = RotateAnimation(
            0f,
            720f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 3000
        rotate.interpolator = LinearInterpolator()
        binding.imageLogo.startAnimation(rotate)
        rotate.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit

            override fun onAnimationRepeat(animation: Animation?) = Unit

            override fun onAnimationEnd(animation: Animation?) {
                viewModel.getOnboardingStatus()
            }
        })
    }

    private fun setUpObserver() {
        viewModel.state.observe(this) { isOnboardingCompleted ->
            intent = if (isOnboardingCompleted == true) {
                Intent(applicationContext, MainActivity::class.java)
            } else {
                Intent(applicationContext, OnboardingActivity::class.java)
            }
            finish()
            startActivity(intent)
        }
    }

}