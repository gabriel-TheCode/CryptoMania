package com.thecode.cryptomania.presentation.main.home

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
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.databinding.AdapterTopCryptoBinding
import com.thecode.cryptomania.utils.extensions.addPrefix
import com.thecode.cryptomania.utils.extensions.addSuffix
import kotlin.math.min

interface CoinCardOnClickListener {
    fun openCoinDetails(coin: CoinItem)
}

class CoinCardRecyclerViewAdapter(private val listener: CoinCardOnClickListener) :
        RecyclerView.Adapter<CoinCardRecyclerViewAdapter.CoinViewHolder>() {

    private lateinit var binding: AdapterTopCryptoBinding
    private var coinsList: List<CoinItem> = listOf()
    private val limit = 10

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        binding =
                AdapterTopCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return min(coinsList.size, limit)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coinsList[position]
        holder.tvCoinName.text = coin.name
        holder.tvCoinPrice.text = coin.current_price.toString().addPrefix("$")

        if (coin.price_change_percentage_24h > 0)
            holder.tvCoinPercentage.setTextColor(
                    ContextCompat.getColor(
                            holder.container.context,
                            R.color.md_green_400
                    )
            ) else holder.tvCoinPercentage.setTextColor(
                ContextCompat.getColor(
                        holder.container.context,
                        R.color.md_red_400
                )
        )
        val percent = String.format("%.2f", coin.price_change_percentage_24h)
        holder.tvCoinPercentage.text = percent.addSuffix("%")

        Glide.with(holder.itemView.context).load(coin.image)
                .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
                .error(R.drawable.ic_baseline_monetization_on_gray_24)
                .apply(RequestOptions().centerCrop())
                .into(holder.image)

        holder.container.setOnClickListener {
            listener.openCoinDetails(coin)
        }
    }

    fun setCoinListItems(coinsList: List<CoinItem>) {
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
}
