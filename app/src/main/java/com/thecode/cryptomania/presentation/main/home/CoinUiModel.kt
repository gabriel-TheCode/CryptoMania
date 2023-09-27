package com.thecode.cryptomania.presentation.main.home

import android.os.Parcelable
import com.thecode.cryptomania.core.domain.CoinItemDomainModel
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

fun List<CoinItemDomainModel>.toUiModels(): List<CoinItemUiModel> {
    return this.map {
        CoinItemUiModel(
            id = it.id,
            symbol = it.symbol,
            name = it.name,
            image = it.image,
            currentPrice = it.currentPrice,
            marketCap = it.marketCap,
            marketCapRank = it.marketCapRank,
            fullyDilutedValuation = it.fullyDilutedValuation,
            totalVolume = it.totalVolume,
            high24h = it.high24h,
            low24h = it.low24h,
            priceChange24h = it.priceChange24h,
            priceChangePercentage24h = it.priceChangePercentage24h,
            marketCapChange24h = it.marketCapChange24h,
            marketCapChangePercentage24h = it.marketCapChangePercentage24h,
            ath = it.ath,
            maxSupply = it.maxSupply
        )
    }
}