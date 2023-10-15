package com.thecode.cryptomania.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.thecode.cryptomania.R
import com.thecode.cryptomania.databinding.ActivitySplashBinding
import com.thecode.cryptomania.presentation.main.MainActivity
import com.thecode.cryptomania.presentation.onboarding.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val viewModel: SplashViewModel by viewModels()
    private val activityScope = CoroutineScope(Dispatchers.Main)

    private lateinit var binding: ActivitySplashBinding
    private lateinit var logo: ImageView
    private lateinit var layoutImage: LinearLayout
    private lateinit var layouTitle: LinearLayout
    private lateinit var upToDown: Animation
    private lateinit var downToUp: Animation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        logo = binding.imageLogo
        layoutImage = binding.layoutImageContent
        layouTitle = binding.layoutText
        downToUp = AnimationUtils.loadAnimation(this, R.anim.down_to_up)
        upToDown = AnimationUtils.loadAnimation(this, R.anim.up_to_down)

        layoutImage.animation = upToDown
        layouTitle.animation = downToUp


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
        logo.startAnimation(rotate)

        activityScope.launch {
            delay(4500)
            val intent: Intent = if (viewModel.isOnboardingCompleted()) {
                Intent(applicationContext, OnboardingActivity::class.java)
            } else {
                Intent(applicationContext, MainActivity::class.java)
            }
            finish()
            startActivity(intent)

        }
    }
}