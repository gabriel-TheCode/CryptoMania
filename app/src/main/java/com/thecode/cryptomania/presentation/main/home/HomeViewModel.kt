package com.thecode.cryptomania.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.usecases.GetCoins
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCoins: GetCoins,
    private val coinItemDomainToUiModelMapper: CoinItemDomainToUiModelMapper
) : ViewModel() {

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
}