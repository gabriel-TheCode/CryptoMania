package com.thecode.cryptomania.base

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.core.domain.ExchangeItem
import com.thecode.cryptomania.presentation.coindetails.CoinDetailsActivity

open class BaseFragment : Fragment() {

    fun showErrorDialog(title: String, description: String) {
        AestheticDialog.Builder(requireActivity(), DialogStyle.RAINBOW, DialogType.ERROR)
            .setTitle(title)
            .setMessage(description)
            .setDuration(2000)
            .show()
    }

    fun showSuccessDialog(title: String, description: String) {
        AestheticDialog.Builder(requireActivity(), DialogStyle.RAINBOW, DialogType.SUCCESS)
            .setTitle(title)
            .setMessage(description)
            .setDuration(2000)
            .show()
    }

    fun openCoinDetailsActivity(coin: CoinItem) {

        val i = Intent(context, CoinDetailsActivity::class.java)

        // ADD DATA TO OUR INTENT
        i.putExtra("id", coin.id)
        i.putExtra("name", coin.name)
        i.putExtra("symbol", coin.symbol)
        i.putExtra("image", coin.image)
        i.putExtra("currentPrice", coin.current_price)
        i.putExtra("high24h", coin.high_24h)
        i.putExtra("low24h", coin.low_24h)
        i.putExtra("marketCap", coin.market_cap)
        i.putExtra("totalVolume", coin.total_volume)
        i.putExtra("priceChangePercentage24h", coin.price_change_percentage_24h)
        i.putExtra("priceMcapChangePercentage24h", coin.market_cap_change_percentage_24h)
        i.putExtra("ath", coin.ath)
        i.putExtra("maxSupply", coin.max_supply)

        // START DETAIL ACTIVITY
        requireActivity().startActivity(i)

        //val intent = Intent(context, CoinDetailsActivity::class.java)
        //intent.putExtra("coin", coin)
        //requireActivity().startActivity(intent)
    }

    fun openExchangeDetailsActivity(exchange: ExchangeItem) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(exchange.url)
            )
        )
    }
}
