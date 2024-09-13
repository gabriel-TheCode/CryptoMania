package com.thecode.cryptomania.core.usecases

import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.domain.ExchangeDomainModel
import com.thecode.cryptomania.core.repositories.ExchangeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetExchanges @Inject constructor(
    private val repository: ExchangeRepository
) {
    suspend operator fun invoke(): Flow<DataState<ExchangeDomainModel>> = flow {
        emit(DataState.Loading)
        try {
            val data = repository.fetchExchanges()
            if (data.exchanges.isEmpty()) {
                emit(DataState.Error(Exception("No result found")))
            } else {
                emit(DataState.Success(data))
            }

        } catch (e: Exception) {
            emit(DataState.Error(Exception(e.message)))
        }
    }.flowOn(Dispatchers.IO)
}