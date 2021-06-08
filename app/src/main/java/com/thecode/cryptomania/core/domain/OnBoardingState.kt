package com.thecode.cryptomania.core.domain

sealed class OnBoardingState {
    data class COMPLET(val list: List<OnBoardingPart>) : OnBoardingState()
}
