package com.thecode.cryptomania.presentation.main.markets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.ExchangeItemDomainModel
import com.thecode.cryptomania.databinding.AdapterExchangeCryptoBinding

interface ExchangeOnClickListener {
    fun openExchangeDetails(exchange: ExchangeItemDomainModel)
}

class ExchangeRecyclerViewAdapter(private val listener: ExchangeOnClickListener) :
    RecyclerView.Adapter<ExchangeRecyclerViewAdapter.ExchangeViewHolder>() {
    private lateinit var binding: AdapterExchangeCryptoBinding
    private var exchangesList: List<ExchangeItemDomainModel> = listOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExchangeViewHolder {
        binding = AdapterExchangeCryptoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExchangeViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return exchangesList.size
    }

    override fun onBindViewHolder(holder: ExchangeViewHolder, position: Int) {
        val exchange = exchangesList[position]
        holder.apply {
            tvExchangeName.text = exchange.name
            tvYear.text = exchange.yearEstablished.toString()
            tvTrustScore.text = exchange.trustScore.toString()
            tvRank.text = exchange.trustScoreRank.toString()
            tvLocation.text = exchange.country

            Glide.with(holder.itemView.context).load(exchange.image)
                .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
                .error(R.drawable.ic_baseline_monetization_on_gray_24)
                .apply(RequestOptions().centerCrop())
                .into(image)

            container.setOnClickListener {
                listener.openExchangeDetails(exchange)
            }
        }
    }

    fun setExchangeListItems(exchangesList: ArrayList<ExchangeItemDomainModel>) {
        this.exchangesList = emptyList()
        this.exchangesList = exchangesList
        notifyDataSetChanged()
    }

    class ExchangeViewHolder(binding: AdapterExchangeCryptoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val container = binding.layoutContainer
        val tvExchangeName = binding.textName
        val tvRank = binding.textRank
        val tvYear = binding.textYear
        val tvTrustScore = binding.textTrustScore
        val tvLocation = binding.textLocation
        val image = binding.imgIcon
    }
}
