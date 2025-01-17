package com.thecode.cryptomania.presentation.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.core.domain.OnBoardingState
import com.thecode.cryptomania.core.usecases.GetOnBoardingParts
import com.thecode.cryptomania.core.usecases.SetOnboardingCompleted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setOnboardingCompleted: SetOnboardingCompleted,
    private val getOnBoardingParts: GetOnBoardingParts
) : ViewModel() {

    private val _state = MutableLiveData<OnBoardingState>()
    val state: LiveData<OnBoardingState>
        get() = _state

    fun getOnBoardingSlide() {
        viewModelScope.launch {
            _state.value = OnBoardingState.Complete(getOnBoardingParts())
        }
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            setOnboardingCompleted.invoke()
        }
    }
}
