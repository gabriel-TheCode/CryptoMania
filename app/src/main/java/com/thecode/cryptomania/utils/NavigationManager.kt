package com.thecode.cryptomania.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.thecode.cryptomania.core.domain.ExchangeItemDomainModel
import com.thecode.cryptomania.presentation.coindetails.CoinDetailsActivity
import com.thecode.cryptomania.presentation.main.home.CoinItemUiModel
import com.thecode.cryptomania.presentation.main.markets.MarketsFragment.Companion.COIN_UI_MODEL

class NavigationManager {

    fun openCoinDetails(context: Context, coin: CoinItemUiModel) {
        val intent = Intent(context, CoinDetailsActivity::class.java)
        intent.putExtra(COIN_UI_MODEL, coin)
        context.startActivity(intent)
    }

    fun openExchangeDetailsActivity(context: Context, exchange: ExchangeItemDomainModel) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(exchange.url)
            )
        )
    }
}