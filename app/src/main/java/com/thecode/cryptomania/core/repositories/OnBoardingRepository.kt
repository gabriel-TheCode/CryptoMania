package com.thecode.cryptomania.core.repositories

import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.OnBoardingPart
import com.thecode.cryptomania.core.local.AppLocalDataSourceImpl
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnBoardingRepository @Inject constructor(
    private val localDataSource: AppLocalDataSourceImpl
) {

    suspend fun setOnboardingCompleted() {
        localDataSource.setOnboardingCompleted()
    }

    fun isOnboardingCompleted(): Flow<Boolean> {
        return localDataSource.isOnboardingCompleted()
    }

    fun getOnBoardingList(): List<OnBoardingPart> {
        return listOf(
            OnBoardingPart(
                image = R.raw.lottie_money,
                title = R.string.title_onboarding_1,
                description = R.string.description_onboarding_1
            ),
            OnBoardingPart(
                image = R.raw.lottie_money_circle,
                title = R.string.title_onboarding_2,
                description = R.string.description_onboarding_2
            ),
            OnBoardingPart(
                image = R.raw.lottie_market_analyst,
                title = R.string.title_onboarding_3,
                description = R.string.description_onboarding_3
            )
        )
    }
}
