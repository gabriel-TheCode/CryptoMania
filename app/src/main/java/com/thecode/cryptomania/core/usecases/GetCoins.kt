package com.thecode.cryptomania.core.usecases

import com.thecode.cryptomania.core.domain.CoinDomainModel
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.repositories.CoinsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetCoins @Inject constructor(
    private val repository: CoinsRepository
) {
    suspend operator fun invoke(currency: String): Flow<DataState<CoinDomainModel>> = flow {
        emit(DataState.Loading)
        try {
            val data = repository.fetchCoins(currency)
            if (data.coins.isEmpty()) {
                emit(DataState.Error(Exception("No result found")))
            } else {
                emit(DataState.Success(data))
            }

        } catch (e: Exception) {
            emit(DataState.Error(Exception(e.message)))
        }
    }.flowOn(Dispatchers.IO)
}
