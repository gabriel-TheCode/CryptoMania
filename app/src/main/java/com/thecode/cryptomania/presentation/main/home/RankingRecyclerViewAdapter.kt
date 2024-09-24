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
import com.thecode.cryptomania.databinding.AdapterRankingCryptoBinding
import com.thecode.cryptomania.utils.extensions.addPrefix
import com.thecode.cryptomania.utils.extensions.addSuffix
import kotlin.math.min


class RankingRecyclerViewAdapter(private val onOpenCoinDetails: (coin: CoinItemUiModel) -> Unit) :
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
        holder.apply {
            val coin = coinsList[position]

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

            tvCoinName.text = coin.name
            tvCoinSymbol.text = coin.symbol
            tvCoinPrice.setTextColor(ContextCompat.getColor(container.context, colorRes))
            tvCoinPrice.text = coin.currentPrice.toString().addPrefix("$")
            layoutPercentage.setBackgroundResource(backgroundRes)
            tvCoinPercentage.text =
                String.format("%s%.2f", sign, coin.priceChangePercentage24h).addSuffix("%")

            Glide.with(itemView.context).load(coin.image)
                .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
                .error(R.drawable.ic_baseline_monetization_on_gray_24)
                .apply(RequestOptions().centerCrop())
                .into(image)

            container.setOnClickListener {
                onOpenCoinDetails(coin)
            }
        }
    }

    fun setCoinListItems(coinsList: List<CoinItemUiModel>) {
        this.coinsList = emptyList()
        this.coinsList = coinsList
        notifyDataSetChanged()
    }

    class CoinViewHolder(binding: AdapterRankingCryptoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val container: RelativeLayout = binding.layoutContainer
        val tvCoinName: TextView = binding.textName
        val tvCoinSymbol: TextView = binding.textSymbol
        val tvCoinPrice: TextView = binding.textPrice
        val layoutPercentage: RelativeLayout = binding.layoutPercent
        val tvCoinPercentage: TextView = binding.textPercentage
        val image: ImageView = binding.imgIcon
    }
}
