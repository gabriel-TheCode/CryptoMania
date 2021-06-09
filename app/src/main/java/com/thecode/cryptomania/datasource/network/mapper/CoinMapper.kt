package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.core.domain.Coin
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.datasource.network.model.CoinObjectResponse
import javax.inject.Inject

class CoinMapper @Inject constructor() :
    EntityMapper<CoinObjectResponse, Coin> {

    override fun mapToDomain(entity: List<CoinObjectResponse>): Coin {
       return Coin(
           entity.map {
               mapFromExchangeItem(it)
           }
       )
    }


    private fun mapFromExchangeItem(coin: CoinObjectResponse): CoinItem {
        return CoinItem(
            coin.id,
            coin.symbol,
            coin.name,
            coin.image,
            coin.current_price,
            coin.market_cap,
            coin.market_cap_rank,
            coin.fully_diluted_valuation,
            coin.total_volume,
            coin.high_24h,
            coin.low_24h,
            coin.price_change_24h,
            coin.price_change_percentage_24h,
            coin.market_cap_change_24h,
            coin.market_cap_change_percentage_24h,
        )
    }


    override fun mapToEntity(domainModel: Coin): CoinObjectResponse {
        TODO("Not yet implemented")
    }
}
