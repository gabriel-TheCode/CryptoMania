package com.thecode.cryptomania.presentation.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.CoinItemDomainModel
import com.thecode.cryptomania.databinding.AdapterTopCryptoBinding
import com.thecode.cryptomania.utils.extensions.addPrefix
import com.thecode.cryptomania.utils.extensions.addSuffix
import kotlin.math.min


class CoinCardRecyclerViewAdapter(private val onOpenCoinDetails: (coin: CoinItemDomainModel) -> Unit) :
    RecyclerView.Adapter<CoinCardRecyclerViewAdapter.CoinViewHolder>() {

    private lateinit var binding: AdapterTopCryptoBinding
    private var coinsList: List<CoinItemDomainModel> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        binding =
            AdapterTopCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return min(coinsList.size, MAX_COINS)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        holder.apply {
            val percent: String
            val coin = coinsList[position]

            if (coin.priceChangePercentage24h > 0) {
                tvCoinPercentage.setTextColor(
                    ContextCompat.getColor(
                        holder.container.context, R.color.md_green_400
                    )
                )
                percent = String.format("+%.2f", coin.priceChangePercentage24h)
            } else {
                tvCoinPercentage.setTextColor(
                    ContextCompat.getColor(
                        holder.container.context, R.color.md_red_400
                    )
                )
                percent = String.format("%.2f", coin.priceChangePercentage24h)
            }

            tvCoinName.text = coin.name
            tvCoinPrice.text = coin.currentPrice.toString().addPrefix("$")
            tvCoinPercentage.text = percent.addSuffix("%")

            Glide.with(holder.itemView.context).load(coin.image)
                .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
                .error(R.drawable.ic_baseline_monetization_on_gray_24)
                .apply(RequestOptions().centerCrop()).into(holder.image)

            container.setOnClickListener {
                onOpenCoinDetails(coin)
            }
        }
    }

    fun setCoinListItems(coinsList: List<CoinItemDomainModel>) {
        this.coinsList = emptyList()
        this.coinsList = coinsList
        notifyDataSetChanged()
    }

    class CoinViewHolder(binding: AdapterTopCryptoBinding) : RecyclerView.ViewHolder(binding.root) {
        val container = binding.layoutContainer
        val tvCoinName = binding.textNameCoin
        val tvCoinPrice = binding.textPrice
        val tvCoinPercentage = binding.textPercentage
        val image = binding.iconCoin
    }

    companion object {
        private const val MAX_COINS = 10
    }
}
