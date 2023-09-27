package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.core.domain.ExchangeDomainModel
import com.thecode.cryptomania.core.domain.ExchangeItemDomainModel
import com.thecode.cryptomania.datasource.network.model.ExchangeObjectResponse
import javax.inject.Inject

class ExchangeMapper @Inject constructor() :
    EntityMapper<ExchangeObjectResponse, ExchangeItemDomainModel> {

    fun mapFromList(entity: List<ExchangeObjectResponse>): ExchangeDomainModel {
        return ExchangeDomainModel(
            entity.map {
                mapToDomain(it)
            }
        )
    }


    override fun mapToDomain(entity: ExchangeObjectResponse): ExchangeItemDomainModel {
        return ExchangeItemDomainModel(
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

    override fun mapToEntity(domainModel: ExchangeItemDomainModel): ExchangeObjectResponse {
        TODO("Not yet implemented")
    }
}
