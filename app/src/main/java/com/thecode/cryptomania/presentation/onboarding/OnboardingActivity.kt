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
        val view = binding.root
        setContentView(view)
        setUpViews()
        setUpObserver()
        viewModel.getOnBoardingSlide()
    }

    private fun setUpViews() {
        binding.apply {
            btnNextStep.setOnClickListener {
                if (getNextItem() > getAdapterSize()) {
                    launchMainScreen()
                } else {
                    viewPager.setCurrentItem(getNextItem(), true)
                }
            }

            textSkip.setOnClickListener {
                viewPager.currentItem = getAdapterSize()
            }
        }
        setUpPager()
    }

    private fun setUpPager() {
        binding.apply {
            viewPager.apply {
                adapter = onBoardingAdapter
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        btnNextStep.text =
                            if (position == 2) getString(R.string.finish) else getString(R.string.next)
                    }
                })
            }
            TabLayoutMediator(pageIndicator, viewPager) { _, _ -> }.attach()
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
        viewModel.setOnboardingCompleted()
        val intent = Intent(applicationContext, OnboardingFinishActivity::class.java)
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
