package com.thecode.cryptomania.presentation.main.markets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.thecode.cryptomania.R
import com.thecode.cryptomania.base.BaseFragment
import com.thecode.cryptomania.core.domain.CoinItemDomainModel
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.domain.ExchangeItemDomainModel
import com.thecode.cryptomania.databinding.FragmentMarketsBinding
import com.thecode.cryptomania.utils.AppConstants.DEFAULT_CURRENCY
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter


@AndroidEntryPoint
class MarketsFragment : BaseFragment() {

    // ViewModel
    private val viewModel: MarketViewModel by viewModels()

    // View Binding
    private var _binding: FragmentMarketsBinding? = null
    private val binding get() = _binding!!

    // Views
    lateinit var marketRecyclerAdapter: MarketRecyclerViewAdapter
    lateinit var exchangeRecyclerAdapter: ExchangeRecyclerViewAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketsBinding.inflate(inflater, container, false)

        subscribeObservers()
        initViews()
        initRecyclerViews()
        fetchMarkets()

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchMarkets() {
        viewModel.getCoins(DEFAULT_CURRENCY)
        binding.recyclerviewCryptoMarkets.scheduleLayoutAnimation()
    }

    private fun fetchExchanges() {
        viewModel.getExchanges()
        binding.recyclerviewCryptoExchanges.scheduleLayoutAnimation()
    }

    private fun showInternetConnectionErrorLayout() {
        binding.apply {
            if (marketRecyclerAdapter.itemCount > 0 || exchangeRecyclerAdapter.itemCount > 0) {
                showErrorDialog(
                    getString(R.string.network_error),
                    getString(R.string.check_internet)
                )
            } else {
                included.apply {
                    layoutBadState.isVisible = true
                    textState.text = getString(R.string.internet_connection_error)
                    btnRetry.isVisible = true
                }
            }
        }
    }

    private fun showBadStateLayout() {
        if (marketRecyclerAdapter.itemCount > 0 || exchangeRecyclerAdapter.itemCount > 0) {
            showErrorDialog(
                getString(R.string.error),
                getString(R.string.service_unavailable)
            )
        } else {
            binding.included.apply {
                layoutBadState.isVisible = true
                textState.text = getString(R.string.no_result_found)
                btnRetry.isVisible = true
            }
        }
    }

    private fun showRecyclerViewMarket(state: Boolean) {
        binding.recyclerviewCryptoMarkets.isVisible = state
        binding.recyclerviewCryptoExchanges.isVisible = !state
    }


    private fun hideBadStateLayout() {
        binding.included.layoutBadState.isVisible = false
    }

    private fun subscribeObservers() {
        viewModel.coinState.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    is DataState.Success -> {
                        hideBadStateLayout()
                        hideLoadingProgress()
                        showRecyclerViewMarket(true)
                        populateRecyclerViewMarket(it.data.coins)
                    }

                    is DataState.Loading -> {
                        showLoadingProgress()
                    }

                    is DataState.Error -> {
                        hideLoadingProgress()
                        showInternetConnectionErrorLayout()
                        Toast.makeText(
                            activity,
                            getString(R.string.internet_connection_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )

        viewModel.exchangeState.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    is DataState.Success -> {
                        hideBadStateLayout()
                        hideLoadingProgress()
                        showRecyclerViewMarket(false)
                        populateRecyclerViewExchange(it.data.exchanges)
                    }

                    is DataState.Loading -> {
                        showLoadingProgress()
                    }

                    is DataState.Error -> {
                        hideLoadingProgress()
                        showInternetConnectionErrorLayout()
                        Toast.makeText(
                            activity,
                            getString(R.string.internet_connection_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    private fun hideLoadingProgress() {
        binding.refreshLayout.isRefreshing = false
    }

    private fun showLoadingProgress() {
        binding.refreshLayout.isRefreshing = true
    }

    private fun initRecyclerViews() {

        binding.apply {
            //region Crypto Market
            marketRecyclerAdapter = MarketRecyclerViewAdapter(onOpenCoinDetails = {
                openCoinDetails(it)
            })
            recyclerviewCryptoMarkets.apply {
                layoutManager = LinearLayoutManager(activity)
                addItemDecoration(
                    DividerItemDecoration(
                        activity,
                        LinearLayoutManager.VERTICAL
                    )
                )
                adapter = SlideInBottomAnimationAdapter(marketRecyclerAdapter)
            }
            //endregion

            //region Exchanges
            exchangeRecyclerAdapter = ExchangeRecyclerViewAdapter(onOpenExchangeDetails = {
                openExchangeDetails(it)
            })
            binding.recyclerviewCryptoExchanges.apply {
                layoutManager = LinearLayoutManager(activity)
                addItemDecoration(
                    DividerItemDecoration(
                        activity,
                        LinearLayoutManager.VERTICAL
                    )
                )
                adapter = SlideInBottomAnimationAdapter(exchangeRecyclerAdapter)

            }
            //endregion
        }
    }

    private fun initViews() {
        binding.apply {
            included.btnRetry.setOnClickListener { if (btnCrypto.isSelected) fetchMarkets() else fetchExchanges() }

            refreshLayout.setOnRefreshListener {
                if (btnCrypto.isSelected) fetchMarkets() else fetchExchanges()
            }

            themedButtonGroup.selectButton(btnCrypto)
            themedButtonGroup.setOnSelectListener {
                when (it) {
                    btnCrypto -> fetchMarkets()
                    btnExchange -> fetchExchanges()
                }
            }
        }

    }

    private fun populateRecyclerViewMarket(coins: List<CoinItemDomainModel>) {
        if (coins.isEmpty()) {
            showBadStateLayout()
        } else {
            val coinArrayList: ArrayList<CoinItemDomainModel> = ArrayList()
            for (i in coins.indices) {
                val coin = coins[i]
                coinArrayList.add(coin)
                marketRecyclerAdapter.setCoinListItems(coinArrayList)
                binding.recyclerviewCryptoMarkets.scheduleLayoutAnimation()
            }
        }
    }

    private fun populateRecyclerViewExchange(exchanges: List<ExchangeItemDomainModel>) {
        if (exchanges.isEmpty()) {
            showBadStateLayout()
        } else {
            val exchangeArrayList: ArrayList<ExchangeItemDomainModel> = ArrayList()
            for (i in exchanges.indices) {
                val exchange = exchanges[i]
                exchangeArrayList.add(exchange)
                exchangeRecyclerAdapter.setExchangeListItems(exchangeArrayList)
                binding.recyclerviewCryptoMarkets.scheduleLayoutAnimation()
            }
        }
    }


    fun openCoinDetails(coin: CoinItemDomainModel) {
        openCoinDetailsActivity(coin)
    }

    fun openExchangeDetails(exchange: ExchangeItemDomainModel) {
        openExchangeDetailsActivity(exchange)
    }


}
