package com.thecode.cryptomania.core.usecases

import com.thecode.cryptomania.core.repositories.OnBoardingRepository
import javax.inject.Inject

class SetOnboardingCompleted @Inject constructor(
    private val repository: OnBoardingRepository
) {
    suspend operator fun invoke() {
        return repository.setOnboardingCompleted()
    }
}
