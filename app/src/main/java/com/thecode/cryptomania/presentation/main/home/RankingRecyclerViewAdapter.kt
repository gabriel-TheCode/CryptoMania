package com.thecode.cryptomania.presentation.main.home


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.thecode.cryptomania.R
import com.thecode.cryptomania.databinding.AdapterRankingCryptoBinding
import com.thecode.cryptomania.utils.extensions.addPrefix
import com.thecode.cryptomania.utils.extensions.addSuffix
import com.thecode.cryptomania.utils.extensions.formatFloat
import kotlin.math.min


class RankingRecyclerViewAdapter(private val listener: CoinCardOnClickListener) :
    RecyclerView.Adapter<RankingRecyclerViewAdapter.CoinViewHolder>() {

    private lateinit var binding: AdapterRankingCryptoBinding
    private var coinsList: List<CoinItemUiModel> = listOf()
    private val limit = 10

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoinViewHolder {
        binding = AdapterRankingCryptoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CoinViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return min(coinsList.size, limit)
    }

    override fun onBindViewHolder(holder: CoinViewHolder, position: Int) {
        val coin = coinsList[position]
        holder.tvCoinName.text = coin.name
        holder.tvCoinSymbol.text = coin.symbol
        val percent = String.format("%.2f", coin.priceChangePercentage24h)

        if (coin.priceChangePercentage24h > 0) {
            holder.tvCoinPrice.setTextColor(
                ContextCompat.getColor(
                    holder.container.context,
                    R.color.md_green_400
                )
            )
            holder.layoutPercentage.setBackgroundResource(R.drawable.rounded_background_green)
            holder.tvCoinPercentage.text = percent.addSuffix("%").addPrefix("+")
        } else {
            holder.layoutPercentage.setBackgroundResource(R.drawable.rounded_background_red)
            holder.tvCoinPrice.setTextColor(
                ContextCompat.getColor(
                    holder.container.context,
                    R.color.md_red_400
                )
            )
            holder.tvCoinPercentage.text = percent.addSuffix("%")
        }
        holder.tvCoinPrice.text = coin.currentPrice.formatFloat().addPrefix("$")

        Glide.with(holder.itemView.context).load(coin.image)
            .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
            .error(R.drawable.ic_baseline_monetization_on_gray_24)
            .apply(RequestOptions().centerCrop())
            .into(holder.image)

        holder.container.setOnClickListener {
            listener.openCoinDetails(coin)
        }
    }

    fun setCoinListItems(coinsList: List<CoinItemUiModel>) {
        this.coinsList = emptyList()
        this.coinsList = coinsList
        notifyDataSetChanged()
    }

    class CoinViewHolder(binding: AdapterRankingCryptoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val container = binding.layoutContainer
        val tvCoinName = binding.textName
        val tvCoinSymbol = binding.textSymbol
        val tvCoinPrice = binding.textPrice
        val layoutPercentage = binding.layoutPercent
        val tvCoinPercentage = binding.textPercentage
        val image = binding.imgIcon
    }
}
