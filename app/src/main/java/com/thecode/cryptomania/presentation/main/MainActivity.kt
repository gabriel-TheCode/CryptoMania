package com.thecode.cryptomania.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thecode.cryptomania.databinding.ActivityMainBinding
import com.thecode.cryptomania.utils.FadePageTransformer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: BottomNavPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        initViews()
        setUpPager()
        setContentView(binding.root)
    }

    private fun initViews() {
        binding.apply {
            bottomNavigationBar.setNavigationChangeListener { _, position ->
                viewPager.setCurrentItem(
                    position,
                    true
                )
            }
            pagerAdapter = BottomNavPagerAdapter(this@MainActivity)
        }

    }

    private fun setUpPager() {
        binding.viewPager.apply {
            offscreenPageLimit = 3
            adapter = pagerAdapter
            isUserInputEnabled = false
            setPageTransformer(FadePageTransformer())
        }

    }
}
