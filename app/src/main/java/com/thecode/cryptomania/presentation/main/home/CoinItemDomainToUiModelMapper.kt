package com.thecode.cryptomania.presentation.main.home

import com.thecode.cryptomania.core.domain.CoinItemDomainModel

class CoinItemDomainToUiModelMapper {

    private fun map(domainModel: CoinItemDomainModel): CoinItemUiModel {
        return CoinItemUiModel(
            id = domainModel.id,
            symbol = domainModel.symbol,
            name = domainModel.name,
            image = domainModel.image,
            currentPrice = domainModel.currentPrice,
            marketCap = domainModel.marketCap,
            marketCapRank = domainModel.marketCapRank,
            fullyDilutedValuation = domainModel.fullyDilutedValuation,
            totalVolume = domainModel.totalVolume,
            high24h = domainModel.high24h,
            low24h = domainModel.low24h,
            priceChange24h = domainModel.priceChange24h,
            priceChangePercentage24h = domainModel.priceChangePercentage24h,
            marketCapChange24h = domainModel.marketCapChange24h,
            marketCapChangePercentage24h = domainModel.marketCapChangePercentage24h,
            ath = domainModel.ath,
            maxSupply = domainModel.maxSupply
        )
    }

    fun toList(domainModels: List<CoinItemDomainModel>): List<CoinItemUiModel> {
        return domainModels.map { map(it) }
    }
}
