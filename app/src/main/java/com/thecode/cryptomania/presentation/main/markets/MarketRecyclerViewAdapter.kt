package com.thecode.cryptomania.presentation.main.markets

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.CoinItemDomainModel
import com.thecode.cryptomania.databinding.AdapterMarketCryptoBinding
import com.thecode.cryptomania.utils.extensions.addPrefix
import com.thecode.cryptomania.utils.extensions.addSuffix
import com.thecode.cryptomania.utils.extensions.withNumberSuffix

class MarketRecyclerViewAdapter(private val onOpenCoinDetails: (coin: CoinItemDomainModel) -> Unit) :
    RecyclerView.Adapter<MarketRecyclerViewAdapter.CoinViewHolder>() {

    private lateinit var binding: AdapterMarketCryptoBinding
    private var coinsList: List<CoinItemDomainModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        binding = AdapterMarketCryptoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CoinViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return coinsList.size
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coinsList[position]

        holder.apply {
            val (colorRes, backgroundRes, sign) = when {
                coin.priceChangePercentage24h > 0 -> Triple(
                    R.color.md_green_400,
                    R.drawable.rounded_background_green,
                    "+"
                )

                else -> Triple(
                    R.color.md_red_400,
                    R.drawable.rounded_background_red,
                    ""
                )
            }

            tvCoinName.text = coin.symbol
            tvMarketCap.text = coin.marketCap.withNumberSuffix().addPrefix("$")
            tvCoinPrice.setTextColor(ContextCompat.getColor(container.context, colorRes))
            tvCoinPrice.text = coin.currentPrice.toString().addPrefix("$")
            layoutPercentage.setBackgroundResource(backgroundRes)
            tvCoinPercentage.text =
                String.format("%s%.2f", sign, coin.priceChangePercentage24h).addSuffix("%")

            Glide.with(holder.itemView.context).load(coin.image)
                .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
                .error(R.drawable.ic_baseline_monetization_on_gray_24)
                .apply(RequestOptions().centerCrop())
                .into(image)

            container.setOnClickListener {
                onOpenCoinDetails(coin)
            }
        }
    }

    fun setCoinListItems(coinsList: ArrayList<CoinItemDomainModel>) {
        this.coinsList = emptyList()
        this.coinsList = coinsList
        notifyDataSetChanged()
    }

    class CoinViewHolder(binding: AdapterMarketCryptoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val container: RelativeLayout = binding.layoutContainer
        val tvCoinName: TextView = binding.textName
        val tvMarketCap: TextView = binding.textCap
        val tvCoinPrice: TextView = binding.textPrice
        val layoutPercentage: RelativeLayout = binding.layoutPercent
        val tvCoinPercentage: TextView = binding.textPercentage
        val image: ImageView = binding.imgIcon
    }
}
