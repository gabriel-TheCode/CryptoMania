package com.thecode.cryptomania.presentation.main.home

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class CoinItemUiModel(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val currentPrice: Float,
    val marketCap: Float,
    val marketCapRank: Int,
    val fullyDilutedValuation: Float?,
    val totalVolume: Float,
    val high24h: Float,
    val low24h: Float,
    val priceChange24h: Float,
    val priceChangePercentage24h: Float,
    val marketCapChange24h: Float,
    val marketCapChangePercentage24h: Float,
    val ath: Float,
    val maxSupply: Float
) : Parcelable