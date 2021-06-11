package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.core.domain.MarketChartItem
import com.thecode.cryptomania.datasource.network.model.MarketChartObjectResponse
import javax.inject.Inject

class MarketChartMapper @Inject constructor() :
    ItemMapper<MarketChartObjectResponse, List<MarketChartItem>> {
    override fun mapToDomain(entity: MarketChartObjectResponse): List<MarketChartItem> {
        return entity.prices[0].map { mapResultItem(it) }
    }

    private fun mapResultItem(m: MarketChartObjectResponse.MarketChart): MarketChartItem {
        return MarketChartItem(
            m.timestamp,
            m.price
        )
    }

    override fun mapToEntity(domainModel: List<MarketChartItem>): MarketChartObjectResponse {
        TODO("Not yet implemented")
    }


}
