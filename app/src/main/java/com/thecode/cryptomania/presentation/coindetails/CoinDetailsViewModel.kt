package com.thecode.cryptomania.presentation.coindetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.usecases.GetMarketChart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDetailsViewModel @Inject constructor(
    private val getMarketChart: GetMarketChart,
) : ViewModel() {

    private val _chartState = MutableLiveData<DataState<List<List<Number>>>>()
    val chartState: LiveData<DataState<List<List<Number>>>>
        get() = _chartState

    fun getMarketChart(coinId: String, currency: String, days: Int) {
        viewModelScope.launch {
            _chartState.value.let { _ ->
                getMarketChart.invoke(coinId, currency, days).collect {
                    _chartState.value = it
                }
            }
        }
    }

}