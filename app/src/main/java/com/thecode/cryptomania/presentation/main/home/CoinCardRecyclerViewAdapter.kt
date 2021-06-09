package com.thecode.cryptomania.presentation.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.databinding.AdapterTopCryptoBinding
import kotlin.math.min

interface CoinCardOnClickListener {

    fun openCoinDetails(coin: CoinItem)

}

class CoinCardRecyclerViewAdapter(private val listener: CoinCardOnClickListener) :
    RecyclerView.Adapter<CoinCardRecyclerViewAdapter.CoinViewHolder>() {

    private lateinit var binding: AdapterTopCryptoBinding
    var coinsList: List<CoinItem> = listOf()
    private val limit = 10

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        binding = AdapterTopCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoinViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return min(coinsList.size, limit)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coinsList[position]
        holder.tvCoinName.text = coin.name
        holder.tvCoinPrice.text = "$" + coin.current_price.toString()
        val percent = String.format("%.2f", coin.price_change_percentage_24h)
        holder.tvCoinPercentage.text = "$percent %"

        Glide.with(holder.itemView.context).load(coin.image)
            .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
            .error(R.drawable.ic_baseline_monetization_on_gray_24)
            .apply(RequestOptions().centerCrop())
            .into(holder.image)
        
        holder.container.setOnClickListener {
            listener.openCoinDetails(coin)
        }
    }

    fun setCoinListItems(coinsList: ArrayList<CoinItem>) {
        this.coinsList = emptyList()
        this.coinsList = coinsList
        notifyDataSetChanged()
    }

    class CoinViewHolder(binding: AdapterTopCryptoBinding) : RecyclerView.ViewHolder(binding.root) {

        val container: RelativeLayout = binding.layoutContainer
        val tvCoinName: TextView = binding.textNameCoin
        val tvCoinPrice: TextView = binding.textPrice
        val tvCoinPercentage: TextView = binding.textPercentage
        val image: ImageView = binding.iconCoin
    }
}
