package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.core.domain.CoinDomainModel
import com.thecode.cryptomania.core.domain.CoinItemDomainModel
import com.thecode.cryptomania.datasource.network.model.CoinObjectResponse
import javax.inject.Inject

class CoinMapper @Inject constructor() :
    EntityMapper<CoinObjectResponse, CoinItemDomainModel> {

    fun mapFromList(entities: List<CoinObjectResponse>): CoinDomainModel {
        return CoinDomainModel(
            entities.map {
                mapToDomain(it)
            }
        )
    }

    override fun mapToEntity(domainModel: CoinItemDomainModel): CoinObjectResponse {
        TODO("Not yet implemented")
    }

    override fun mapToDomain(entity: CoinObjectResponse): CoinItemDomainModel {
        return CoinItemDomainModel(
            entity.id,
            entity.symbol,
            entity.name,
            entity.image,
            entity.currentPrice,
            entity.marketCap,
            entity.marketCapRank,
            entity.fullyDilutedValuation,
            entity.totalVolume,
            entity.high24h,
            entity.low24h,
            entity.priceChange24h,
            entity.priceChangePercentage24h,
            entity.marketCapChange24h,
            entity.marketCapChangePercentage24h,
            entity.ath,
            entity.maxSupply
        )
    }
}
