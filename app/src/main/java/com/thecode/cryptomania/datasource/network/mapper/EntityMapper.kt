package com.thecode.cryptomania.datasource.network.mapper

import com.thecode.cryptomania.datasource.network.model.CoinObjectResponse

interface EntityMapper<Entity, DomainModel> {

    fun mapToDomain(entity: List<CoinObjectResponse>): DomainModel

    fun mapToEntity(domainModel: DomainModel): Entity
}
