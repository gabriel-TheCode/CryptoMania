package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.core.domain.Coin
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.datasource.network.model.CoinObjectResponse
import javax.inject.Inject

class CoinMapper @Inject constructor() :
        EntityMapper<CoinObjectResponse, CoinItem> {

    fun mapFromList(entities: List<CoinObjectResponse>): Coin {
        return Coin(
                entities.map {
                    mapToDomain(it)
                }
        )
    }

    override fun mapToEntity(domainModel: CoinItem): CoinObjectResponse {
        TODO("Not yet implemented")
    }

    override fun mapToDomain(entity: CoinObjectResponse): CoinItem {
        return CoinItem(
                entity.id,
                entity.symbol,
                entity.name,
                entity.image,
                entity.current_price,
                entity.market_cap,
                entity.market_cap_rank,
                entity.fully_diluted_valuation,
                entity.total_volume,
                entity.high_24h,
                entity.low_24h,
                entity.price_change_24h,
                entity.price_change_percentage_24h,
                entity.market_cap_change_24h,
                entity.market_cap_change_percentage_24h,
                entity.ath,
                entity.max_supply
        )
    }
}
