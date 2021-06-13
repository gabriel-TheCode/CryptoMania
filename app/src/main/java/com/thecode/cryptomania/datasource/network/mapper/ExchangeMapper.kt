package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.core.domain.Exchange
import com.thecode.cryptomania.core.domain.ExchangeItem
import com.thecode.cryptomania.datasource.network.model.ExchangeObjectResponse
import javax.inject.Inject

class ExchangeMapper @Inject constructor() :
    EntityMapper<ExchangeObjectResponse, ExchangeItem> {

    fun mapFromList(entity: List<ExchangeObjectResponse>): Exchange {
        return Exchange(
            entity.map {
                mapToDomain(it)
            }
        )
    }


    override fun mapToDomain(entity: ExchangeObjectResponse): ExchangeItem {
        return ExchangeItem(
            entity.id,
            entity.name,
            entity.year_established,
            entity.country,
            entity.description,
            entity.url,
            entity.image,
            entity.has_trading_incentive,
            entity.trust_score,
            entity.trust_score_rank,
            entity.trade_volume_24h_btc,
            entity.trade_volume_24h_btc_normalized
        )
    }

    override fun mapToEntity(domainModel: ExchangeItem): ExchangeObjectResponse {
        TODO("Not yet implemented")
    }
}
