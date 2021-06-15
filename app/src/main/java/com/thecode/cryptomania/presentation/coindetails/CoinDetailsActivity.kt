package com.thecode.cryptomania.presentation.coindetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.core.cartesian.series.Line
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.databinding.ActivityCoinDetailsBinding
import com.thecode.cryptomania.utils.extensions.addPrefix
import com.thecode.cryptomania.utils.extensions.addSuffix
import com.thecode.cryptomania.utils.extensions.withNumberSuffix
import dagger.hilt.android.AndroidEntryPoint
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup
import java.sql.Timestamp


@AndroidEntryPoint
class CoinDetailsActivity : AppCompatActivity() {

    private val viewModel: CoinDetailsViewModel by viewModels()

    private lateinit var binding: ActivityCoinDetailsBinding

    private lateinit var btnRetry: AppCompatButton
    private lateinit var layoutBadState: View
    private lateinit var textState: TextView
    private lateinit var imgState: ImageView
    private lateinit var textCoinName: TextView
    private lateinit var textCoinSymbol: TextView
    private lateinit var textCoinMcap: TextView
    private lateinit var textCoinPrice: TextView
    private lateinit var textCoinChange24h: TextView
    private lateinit var textCoinChange24hTop: TextView
    private lateinit var textCoinMcapChange24h: TextView
    private lateinit var textCoinLow24h: TextView
    private lateinit var textCoinHigh24h: TextView
    private lateinit var textMaxSupply: TextView
    private lateinit var textAth: TextView
    private lateinit var imgCoin: ImageView
    private lateinit var themedButtonGroup: ThemedToggleButtonGroup
    private lateinit var btnDay: ThemedButton
    private lateinit var btnWeek: ThemedButton
    private lateinit var btnMonth: ThemedButton
    private lateinit var anyChartView: AnyChartView
    private lateinit var progressBar: ProgressBar

    private lateinit var set: Set

    private lateinit var id: String
    private lateinit var name: String
    private lateinit var symbol: String
    private lateinit var image: String
    private var currentPrice: Float? = 0F
    private var high24h: Float? = 0F
    private var low24h: Float? = 0F
    private var marketCap: Float? = 0F
    private var totalVolume: Float? = 0F
    private var priceChangePercentage24h: Float? = 0F
    private var priceMCapChange24h: Float? = 0F
    private var ath: Float? = 0F
    private var maxSupply: Float? = 0F
    private var days: Int = 1

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoinDetailsBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        subscribeObservers()

        getCoinData()

        initViews()

        setCoinData()

        fetchChart(days)

    }


    private fun initViews() {
        imgCoin = binding.imgIcon
        textCoinName = binding.textName
        textCoinSymbol = binding.textSymbol
        textCoinMcap = binding.textCoinMcap
        textCoinPrice = binding.textPrice
        textCoinChange24h = binding.textPriceChange24h
        textCoinMcapChange24h = binding.textCapChange24h
        textCoinLow24h = binding.textLow24h
        textCoinHigh24h = binding.textHigh24h
        textCoinChange24hTop = binding.textPriceChange24hTop
        textAth = binding.textAth
        textMaxSupply = binding.textMaxSupply
        imgCoin = binding.imgIcon
        themedButtonGroup = binding.themedButtonGroup
        btnDay = binding.btnDay
        btnWeek = binding.btnWeek
        btnMonth = binding.btnMonth
        anyChartView = binding.layoutChart
        btnRetry = binding.included.btnRetry
        layoutBadState = binding.included.layoutBadState
        imgState = binding.included.imgState
        textState = binding.included.textState
        progressBar = binding.progressBar

        anyChartView.setProgressBar(progressBar)
        anyChartView.setZoomEnabled(true)
        themedButtonGroup.selectButton(btnDay)
        btnRetry.setOnClickListener { fetchChart(days) }

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

    private fun getCoinData() {

        // RECEIVE OUR DATA
        val i = intent
        id = i.extras?.getString("id").toString()
        name = i.extras?.getString("name").toString()
        symbol = i.extras?.getString("symbol").toString()
        image = i.extras?.getString("image").toString()
        currentPrice = i.extras?.getFloat("currentPrice")
        high24h = i.extras?.getFloat("high24h")
        low24h = i.extras?.getFloat("low24h")
        marketCap = i.extras?.getFloat("marketCap")
        totalVolume = i.extras?.getFloat("totalVolume")
        priceChangePercentage24h = i.extras?.getFloat("priceChangePercentage24h")
        priceMCapChange24h = i.extras?.getFloat("priceMcapChangePercentage24h")
        ath = i.extras?.getFloat("ath")
        maxSupply = i.extras?.getFloat("maxSupply")
    }

    private fun setCoinData() {
        Glide.with(this).load(image)
            .placeholder(R.drawable.ic_baseline_monetization_on_gray_24)
            .error(R.drawable.ic_baseline_monetization_on_gray_24)
            .apply(RequestOptions().centerCrop())
            .into(imgCoin)
        textCoinName.text = name
        textCoinPrice.text = currentPrice.toString().addPrefix("$")
        textCoinSymbol.text = symbol
        textCoinLow24h.text = low24h.toString().addPrefix("$")
        textCoinHigh24h.text = high24h.toString().addPrefix("$")
        textCoinMcap.text = marketCap?.withNumberSuffix()?.addPrefix("$") ?: "N/A"
        textCoinChange24h.text = priceChangePercentage24h.toString().addSuffix("%")
        textCoinChange24hTop.text = priceChangePercentage24h.toString().addSuffix("%")
        textCoinMcapChange24h.text = priceMCapChange24h.toString().addSuffix("%")
        textAth.text = ath.toString().addPrefix("$")
        textMaxSupply.text = String.format("%.0f", maxSupply)

        if (priceChangePercentage24h!! < 0) {
            binding.layoutPercent.setBackgroundResource(R.drawable.rounded_background_red)
        }
    }

    private fun fetchChart(days: Int) {
        viewModel.getMarketChart(id, "usd", days)
    }

    private fun subscribeObservers() {
        viewModel.chartState.observe(
            this, {
                when (it) {
                    is DataState.Success -> {
                        hideBadStateLayout()
                        populateChart(it.data)
                    }
                    is DataState.Loading -> {
                        progressBar.isVisible = true
                    }
                    is DataState.Error -> {
                        anyChartView.clear()
                        progressBar.isVisible = false
                        showBadStateLayout()
                        Toast.makeText(
                            this,
                            getString(R.string.internet_connection_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }


    private fun showBadStateLayout() {
        layoutBadState.isVisible = true
        textState.text = getString(R.string.internet_connection_error)
        btnRetry.isVisible = true
    }

    private fun hideBadStateLayout() {
        layoutBadState.isVisible = false
    }


    private fun populateChart(items: List<List<Number>>) {
        var timestamp: Timestamp?
        var price: Number
        val seriesData: MutableList<DataEntry> = ArrayList()

        if (items.isEmpty()) {
            Toast.makeText(
                this,
                getString(R.string.no_result_found),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            for (i in items.indices) {
                val data = items[i]
                timestamp = Timestamp(data[0].toLong())
                price = data[1]

                val date: String = timestamp.toString().substring(0, 19)
                Log.d("Market Chart values", "[$date, $price]")
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

        cartesian.title("Crypto currency fluctuation chart.")

        cartesian.yAxis(0).title("Price (USD)")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)

        val series: Line = cartesian.line(seriesData)
        series.name(name)
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

        anyChartView.setChart(cartesian)


    }
}