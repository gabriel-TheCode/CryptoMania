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
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.databinding.AdapterMarketCryptoBinding
import com.thecode.cryptomania.presentation.main.home.CoinCardOnClickListener
import kotlin.math.ln
import kotlin.math.pow


class MarketRecyclerViewAdapter(private val listener: CoinCardOnClickListener) :
    RecyclerView.Adapter<MarketRecyclerViewAdapter.CoinViewHolder>() {

    private lateinit var binding: AdapterMarketCryptoBinding
    var coinsList: List<CoinItem> = listOf()

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
        holder.tvCoinName.text = coin.symbol
        holder.tvMarketCap.text = withSuffix(coin.market_cap)
        if (coin.price_change_percentage_24h > 0){
            holder.tvCoinPrice.setTextColor(
                ContextCompat.getColor(
                    holder.container.context,
                    R.color.md_green_400
                )
            )
            holder.layoutPercentage.setBackgroundResource(R.drawable.rounded_background_green)

        }else{
            holder.layoutPercentage.setBackgroundResource(R.drawable.rounded_background_red)
            holder.tvCoinPrice.setTextColor(
                ContextCompat.getColor(
                    holder.container.context,
                    R.color.md_red_400
                )
            )
        }
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

    private fun withSuffix(count: Float): String {
        if (count < 1000) return "" + count
        val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
        return String.format(
            "%.1f %c",
            count / 1000.0.pow(exp.toDouble()),
            "kMGTPE"[exp - 1]
        )
    }
    class CoinViewHolder(binding: AdapterMarketCryptoBinding) : RecyclerView.ViewHolder(binding.root) {

        val container: RelativeLayout = binding.layoutContainer
        val tvCoinName: TextView = binding.textName
        val tvMarketCap: TextView = binding.textCap
        val tvCoinPrice: TextView = binding.textPrice
        val layoutPercentage: RelativeLayout = binding.layoutPercent
        val tvCoinPercentage: TextView = binding.textPercentage
        val image: ImageView = binding.imgIcon
    }
}
