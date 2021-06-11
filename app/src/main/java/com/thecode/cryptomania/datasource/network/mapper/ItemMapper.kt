package com.thecode.cryptomania.datasource.network.mapper

interface ItemMapper<Entity, DomainModel> {

    fun mapToDomain(entity: Entity): DomainModel

    fun mapToEntity(domainModel: DomainModel): Entity
}

