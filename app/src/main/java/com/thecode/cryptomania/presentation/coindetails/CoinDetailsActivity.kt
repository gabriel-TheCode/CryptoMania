package com.thecode.cryptomania.presentation.coindetails

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.core.cartesian.series.Line
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.thecode.cryptomania.R
import com.thecode.cryptomania.base.BaseFragment.Companion.COIN_UI_MODEL
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.databinding.ActivityCoinDetailsBinding
import com.thecode.cryptomania.presentation.main.home.CoinItemUiModel
import com.thecode.cryptomania.utils.extensions.addPrefix
import com.thecode.cryptomania.utils.extensions.addSuffix
import com.thecode.cryptomania.utils.extensions.withNumberSuffix
import dagger.hilt.android.AndroidEntryPoint
import java.sql.Timestamp

@AndroidEntryPoint
class CoinDetailsActivity : AppCompatActivity() {
    private val viewModel: CoinDetailsViewModel by viewModels()
    private lateinit var binding: ActivityCoinDetailsBinding
    private var days: Int = 1
    private var coin: CoinItemUiModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoinDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coin = if (supportsAndroid13()) {
            intent.extras?.getParcelable(COIN_UI_MODEL, CoinItemUiModel::class.java)
        } else {
            intent.extras?.getParcelable(COIN_UI_MODEL)
        }

        subscribeObservers()
        initViews()
        setCoinData()
        fetchChart(days)
    }

    private fun initViews() {
        binding.apply {
            layoutChart.setProgressBar(progressBar)
            layoutChart.setZoomEnabled(true)
            themedButtonGroup.selectButton(btnDay)
            included.btnRetry.setOnClickListener { fetchChart(days) }

            themedButtonGroup.setOnSelectListener {
                when (it) {
                    btnDay -> {
                        days = 1
                        fetchChart(days)
                    }

                    btnWeek -> {
                        days = 7
                        fetchChart(days)
                    }

                    btnMonth -> {
                        days = 30
                        fetchChart(days)
                    }
                }
            }
        }
    }

    private fun supportsAndroid13() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun setCoinData() {
        binding.apply {
            coin?.apply {
                Glide.with(this@CoinDetailsActivity).load(image)
                    .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
                    .error(R.drawable.ic_baseline_monetization_on_gray_24)
                    .apply(RequestOptions().centerCrop())
                    .into(coinIconImageView)
                coinNameTextView.text = name
                priceTextView.text = currentPrice.toString().addPrefix("$")
                symbolTextView.text = symbol
                lowPrice24hTextView.text = low24h.toString().addPrefix("$")
                highPrice24hTextView.text = high24h.toString().addPrefix("$")
                coinMarketCapTextView.text = marketCap.withNumberSuffix().addPrefix("$")
                marketCapChange24hTextView.text = "%.0f".format(marketCapChange24h).addSuffix("$")
                athTextView.text = ath.toString().addPrefix("$")
                maxSupplyTextView.text = String.format("%.0f", maxSupply)
                val priceChange = "%.2f".format(priceChangePercentage24h).addSuffix("%")
                if (priceChangePercentage24h < 0) {
                    textPriceChange24hTop.text = priceChange
                    binding.layoutPercent.setBackgroundResource(R.drawable.rounded_background_red)
                } else {
                    textPriceChange24hTop.text = priceChange.addPrefix("+")
                }
                priceChange24hTextView.text = textPriceChange24hTop.text
            }
        }
    }

    private fun fetchChart(days: Int) {
        coin?.id?.let { viewModel.getMarketChart(it, getString(R.string.usd), days) }
    }

    private fun subscribeObservers() {
        viewModel.chartState.observe(
            this
        ) {
            when (it) {
                is DataState.Success -> {
                    hideBadStateLayout()
                    populateChart(it.data)
                }

                is DataState.Loading -> {
                    binding.progressBar.isVisible = true
                }

                is DataState.Error -> {
                    binding.apply {
                        layoutChart.invalidate()
                        progressBar.isVisible = false
                    }
                    showBadStateLayout()
                    Toast.makeText(
                        this,
                        getString(R.string.internet_connection_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showBadStateLayout() {
        binding.included.apply {
            layoutBadState.isVisible = true
            textState.text = getString(R.string.internet_connection_error)
            btnRetry.isVisible = true
        }
    }

    private fun hideBadStateLayout() {
        binding.included.layoutBadState.isVisible = false
    }

    private fun populateChart(items: List<List<Number>>) {
        var timestamp: Timestamp?
        var price: Number
        val seriesData = mutableListOf<DataEntry>()

        if (items.isEmpty()) {
            Toast.makeText(
                this@CoinDetailsActivity,
                getString(R.string.no_result_found),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            for (i in items.indices) {
                val data = items[i]
                timestamp = Timestamp(data[0].toLong())
                price = data[1]
                val date: String = timestamp.toString().substring(0, 19)
                Log.d(getString(R.string.market_chart_values), "[$date, $price]")
                seriesData.add(ValueDataEntry(date, price))
            }
        }
        setUpChart(seriesData)
    }

    private fun setUpChart(seriesData: MutableList<DataEntry>) {
        val cartesian = AnyChart.line()

        cartesian.animation(true)

        cartesian.padding(10.0, 20.0, 5.0, 20.0)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true)
            .yStroke(null as Stroke?, null, null, null as String?, null as String?)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.title(getString(R.string.crypto_currency_fluctuation_chart))

        cartesian.yAxis(0).title(getString(R.string.price_usd))
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)
        val series: Line = cartesian.line(seriesData)
        series.name(coin?.name)
        series.hovered().markers().enabled(true)
        series.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)

        binding.layoutChart.apply {
            handler.run {
                postDelayed({
                    series.data(seriesData)
                }, 500)

                postDelayed({
                    setChart(cartesian)
                }, 500)
            }
        }
    }
}