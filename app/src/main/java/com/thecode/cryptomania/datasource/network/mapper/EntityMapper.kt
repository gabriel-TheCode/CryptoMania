package com.thecode.cryptomania.datasource.network.mapper

interface EntityMapper<Entity, DomainModel> {

    fun mapToDomain(entity: List<Entity>): DomainModel

    fun mapToEntity(domainModel: DomainModel): Entity
}
