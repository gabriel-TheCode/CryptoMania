package com.thecode.cryptomania.datasource.network.mapper


import com.thecode.cryptomania.datasource.network.model.MarketChartObjectResponse
import javax.inject.Inject

class MarketChartMapper @Inject constructor() :
    EntityMapper<MarketChartObjectResponse, List<List<Number>>> {
    override fun mapToDomain(entity: MarketChartObjectResponse): List<List<Number>> {
        return entity.prices
    }

    override fun mapToEntity(domainModel: List<List<Number>>): MarketChartObjectResponse {
        TODO("Not yet implemented")
    }


}
