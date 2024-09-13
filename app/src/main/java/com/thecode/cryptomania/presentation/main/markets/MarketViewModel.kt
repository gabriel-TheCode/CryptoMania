package com.thecode.cryptomania.presentation.main.markets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.core.domain.CoinDomainModel
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.domain.ExchangeDomainModel
import com.thecode.cryptomania.core.usecases.GetCoins
import com.thecode.cryptomania.core.usecases.GetExchanges
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val getCoins: GetCoins,
    private val getExchanges: GetExchanges
) : ViewModel() {

    private val _coinState = MutableLiveData<DataState<CoinDomainModel>>()
    val coinState: LiveData<DataState<CoinDomainModel>>
        get() = _coinState

    private val _exchangeState = MutableLiveData<DataState<ExchangeDomainModel>>()
    val exchangeState: LiveData<DataState<ExchangeDomainModel>>
        get() = _exchangeState

    fun getCoins(currency: String) {
        viewModelScope.launch {
            _coinState.value.let { _ ->
                getCoins.invoke(currency).onEach {
                    _coinState.value = it
                }.launchIn(viewModelScope)
            }
        }
    }


    fun getExchanges() {
        viewModelScope.launch {
            _exchangeState.value.let { _ ->
                getExchanges.invoke().onEach {
                    _exchangeState.value = it
                }.launchIn(viewModelScope)
            }
        }
    }

}