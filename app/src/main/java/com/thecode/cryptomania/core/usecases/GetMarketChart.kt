package com.thecode.cryptomania.core.usecases

import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.repositories.MarketChartRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetMarketChart @Inject constructor(
    private val repository: MarketChartRepository
) {
    suspend operator fun invoke(
        coinId: String,
        currency: String,
        days: Int
    ): Flow<DataState<List<List<Number>>>> = flow {
        emit(DataState.Loading)
        try {
            val data = repository.fetchMarketChartData(coinId, currency, days)
            if (data.isEmpty()) {
                emit(DataState.Error(Exception("No result found")))
            } else {
                emit(DataState.Success(data))
            }

        } catch (e: Exception) {
            emit(DataState.Error(Exception(e.message)))
        }
    }.flowOn(Dispatchers.IO)
}