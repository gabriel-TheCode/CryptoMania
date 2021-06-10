package com.thecode.cryptomania.presentation.coindetails

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.thecode.cryptomania.databinding.ActivityCoinDetailsBinding

class CoinDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoinDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoinDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // RECEIVE OUR DATA
        val i = intent
        val id = i.extras?.getString("id")
        val name = i.extras?.getString("name")
        val symbol = i.extras?.getString("symbol")
        val image = i.extras?.getString("imageUrl")
        val currentPrice = i.extras?.getString("currentPrice")
        val high24h = i.extras?.getString("high24h")
        val marketCap = i.extras?.getString("marketCap")
        val totalVolume = i.extras?.getString("totalVolume")
        val priceChangePercentage24h = i.extras?.getString("priceChangePercentage24h")
    }
}