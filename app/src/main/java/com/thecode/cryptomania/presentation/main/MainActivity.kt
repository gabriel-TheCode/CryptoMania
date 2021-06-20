package com.thecode.cryptomania.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.gauravk.bubblenavigation.BubbleNavigationLinearView
import com.thecode.cryptomania.databinding.ActivityMainBinding
import com.thecode.cryptomania.utils.FadePageTransformer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var bnlv: BubbleNavigationLinearView
    private lateinit var pagerAdapter: BottomNavPagerAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        initViews()
        setUpPager()

        setContentView(view)
    }

    private fun initViews() {
        viewPager = binding.viewPager
        bnlv = binding.bottomNavigationBar
        bnlv.setNavigationChangeListener { _, position ->
            viewPager.setCurrentItem(
                    position,
                    true
            )
        }
        pagerAdapter = BottomNavPagerAdapter(this)
    }

    private fun setUpPager() {
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = pagerAdapter
        viewPager.isUserInputEnabled = false
        viewPager.setPageTransformer(FadePageTransformer())
    }
}
