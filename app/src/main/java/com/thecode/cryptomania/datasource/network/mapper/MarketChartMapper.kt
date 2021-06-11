package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.datasource.network.model.MarketChartObjectResponse
import javax.inject.Inject

class MarketChartMapper @Inject constructor() :
    ItemMapper<MarketChartObjectResponse, List<List<Number>>> {
    override fun mapToDomain(entity: MarketChartObjectResponse): List<List<Number>> {
        return entity.prices

    }

    private fun mapResultItem(m: MarketChartObjectResponse): List<Number> {
        val prices = arrayOf(m.prices[0][0], m.prices[0][1])
        return prices.toList()
    }

    override fun mapToEntity(domainModel: List<List<Number>>): MarketChartObjectResponse {
        TODO("Not yet implemented")
    }


}
