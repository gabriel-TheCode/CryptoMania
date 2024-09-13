package com.thecode.cryptomania.presentation.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
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

    private lateinit var mViewPager: ViewPager2
    private lateinit var btnNext: Button
    private lateinit var pageIndicator: TabLayout
    private lateinit var textSkip: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initViews()
        setUpListeners()
        setUpPager()
        setUpObserver()

        viewModel.getOnBoardingSlide()
    }

    private fun initViews() {
        mViewPager = binding.viewPager
        mViewPager.offscreenPageLimit = 1

        btnNext = binding.btnNextStep
        textSkip = binding.textSkip
        pageIndicator = binding.pageIndicator
    }

    private fun setUpListeners() {

        btnNext.setOnClickListener {
            if (getNextItem() > getAdapterSize()) {
                launchMainScreen()
            } else {
                mViewPager.setCurrentItem(getNextItem(), true)
            }
        }

        textSkip.setOnClickListener {
            mViewPager.currentItem = getAdapterSize()
        }


    }

    private fun setUpPager() {
        mViewPager.adapter = onBoardingAdapter
        TabLayoutMediator(pageIndicator, mViewPager) { _, _ -> }.attach()
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 2) {
                    btnNext.text = getText(R.string.finish)
                } else {
                    btnNext.text = getText(R.string.next)
                }
            }
        })
    }

    private fun setUpObserver() {
        viewModel.state.observe(
            this,
            {
                when (it) {
                    is OnBoardingState.Complete -> onBoardingAdapter.setItem(it.list)
                }
            }
        )
    }

    private fun launchMainScreen() {
        viewModel.setOnboardingCompleted()
        val intent = Intent(applicationContext, OnboardingFinishActivity::class.java)
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_from_right)
        startActivity(intent)
        finishAffinity()
    }

    private fun getNextItem(): Int {
        return mViewPager.currentItem + 1
    }

    private fun getAdapterSize(): Int {
        return onBoardingAdapter.itemCount - 1
    }
}
