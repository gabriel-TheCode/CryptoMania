package com.thecode.cryptomania.base

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import com.thecode.cryptomania.core.domain.CoinItemDomainModel
import com.thecode.cryptomania.core.domain.ExchangeItemDomainModel
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

    fun openCoinDetailsActivity(coin: CoinItemDomainModel) {

        val i = Intent(context, CoinDetailsActivity::class.java)

        // ADD DATA TO OUR INTENT
        i.putExtra("id", coin.id)
        i.putExtra("name", coin.name)
        i.putExtra("symbol", coin.symbol)
        i.putExtra("image", coin.image)
        i.putExtra("currentPrice", coin.currentPrice)
        i.putExtra("high24h", coin.high24h)
        i.putExtra("low24h", coin.low24h)
        i.putExtra("marketCap", coin.marketCap)
        i.putExtra("totalVolume", coin.totalVolume)
        i.putExtra("priceChangePercentage24h", coin.priceChangePercentage24h)
        i.putExtra("priceMcapChangePercentage24h", coin.marketCapChangePercentage24h)
        i.putExtra("ath", coin.ath)
        i.putExtra("maxSupply", coin.maxSupply)

        // START DETAIL ACTIVITY
        requireActivity().startActivity(i)

        //val intent = Intent(context, CoinDetailsActivity::class.java)
        //intent.putExtra("coin", coin)
        //requireActivity().startActivity(intent)
    }

    fun openExchangeDetailsActivity(exchange: ExchangeItemDomainModel) {
        startActivity(
                Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(exchange.url)
                )
        )
    }
}
