package com.thecode.cryptomania.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.core.domain.CoinDomainModel
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.usecases.GetCoins
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCoins: GetCoins
) : ViewModel() {

    private val _coinState = MutableLiveData<DataState<CoinDomainModel>>()
    val coinState: LiveData<DataState<CoinDomainModel>>
        get() = _coinState

    fun getCoins(currency: String) {
        viewModelScope.launch {
            _coinState.value.let { _ ->
                getCoins.invoke(currency).onEach {
                    _coinState.value = it
                }.launchIn(viewModelScope)
            }
        }
    }
}