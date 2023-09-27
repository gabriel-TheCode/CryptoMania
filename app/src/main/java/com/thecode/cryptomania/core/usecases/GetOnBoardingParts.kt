package com.thecode.cryptomania.core.usecases

import com.thecode.cryptomania.core.domain.OnBoardingPart
import com.thecode.cryptomania.core.repositories.OnBoardingRepository
import javax.inject.Inject

class GetOnBoardingParts @Inject constructor(
    private val repository: OnBoardingRepository
) {
    operator fun invoke(): List<OnBoardingPart> {
        return repository.getOnBoardingList()
    }
}
