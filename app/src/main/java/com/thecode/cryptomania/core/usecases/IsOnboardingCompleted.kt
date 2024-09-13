package com.thecode.cryptomania.core.usecases

import com.thecode.cryptomania.core.repositories.OnBoardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsOnboardingCompleted @Inject constructor(
    private val repository: OnBoardingRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.isOnboardingCompleted()
    }
}
