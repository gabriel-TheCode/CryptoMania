package com.thecode.cryptomania.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.OnBoardingState
import com.thecode.cryptomania.databinding.ActivityOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private val viewModel: OnboardingViewModel by viewModels()
    private val onBoardingAdapter = OnBoardingAdapter()
    private lateinit var binding: ActivityOnboardingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setUpObserver()

        viewModel.getOnBoardingSlide()
    }

    private fun initViews() {
        binding.apply {
            viewPager.offscreenPageLimit = 1
            viewPager.adapter = onBoardingAdapter
            TabLayoutMediator(pageIndicator, viewPager) { _, _ -> }.attach()
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == 2) {
                        btnNextStep.text = getText(R.string.finish)
                    } else {
                        btnNextStep.text = getText(R.string.next)
                    }
                }
            })

            btnNextStep.setOnClickListener {
                if (getNextItem() > getAdapterSize()) {
                    viewModel.setOnboardingCompleted()
                    launchMainScreen()
                } else {
                    viewPager.setCurrentItem(getNextItem(), true)
                }
            }

            textSkip.setOnClickListener {
                viewPager.currentItem = getAdapterSize()
            }
        }
    }

    private fun setUpObserver() {
        viewModel.state.observe(
            this
        ) {
            when (it) {
                is OnBoardingState.Complete -> onBoardingAdapter.setItem(it.list)
            }
        }
    }

    private fun launchMainScreen() {
        val intent = Intent(applicationContext, OnboardingFinishActivity::class.java)
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right)
        startActivity(intent)
        finishAffinity()
    }

    private fun getNextItem(): Int {
        return binding.viewPager.currentItem + 1
    }

    private fun getAdapterSize(): Int {
        return onBoardingAdapter.itemCount - 1
    }
}
