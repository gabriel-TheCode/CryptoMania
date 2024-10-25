package com.thecode.cryptomania.presentation.main.markets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.domain.ExchangeDomainModel
import com.thecode.cryptomania.core.usecases.GetCoins
import com.thecode.cryptomania.core.usecases.GetExchanges
import com.thecode.cryptomania.presentation.main.home.CoinItemDomainToUiModelMapper
import com.thecode.cryptomania.presentation.main.home.CoinItemUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getCoins: GetCoins,
    private val getExchanges: GetExchanges,
    private val coinItemDomainToUiModelMapper: CoinItemDomainToUiModelMapper
) : ViewModel() {

    private val _exchangeState = MutableLiveData<DataState<ExchangeDomainModel>>()
    val exchangeState: LiveData<DataState<ExchangeDomainModel>>
        get() = _exchangeState


    private val _coinState = MutableLiveData<DataState<List<CoinItemUiModel>>>()
    val coinState: LiveData<DataState<List<CoinItemUiModel>>>
        get() = _coinState


    fun getCoins(currency: String) {
        viewModelScope.launch {
            getCoins.invoke(currency).collect { dataState ->
                val uiDataState = when (dataState) {
                    is DataState.Success -> {
                        val uiModelList =
                            coinItemDomainToUiModelMapper.toList(dataState.data.coins)
                        DataState.Success(uiModelList)
                    }

                    is DataState.Error -> {
                        DataState.Error(dataState.exception)
                    }

                    is DataState.Loading -> {
                        DataState.Loading
                    }
                }
                _coinState.value = uiDataState
            }
        }
    }


    fun getExchanges() {
        viewModelScope.launch {
            getExchanges.invoke().onEach {
                _exchangeState.value = it
            }.launchIn(viewModelScope)
        }
    }
}