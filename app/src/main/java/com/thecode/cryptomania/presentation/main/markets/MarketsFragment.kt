package com.thecode.cryptomania.presentation.main.markets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.core.domain.ExchangeItemDomainModel
import com.thecode.cryptomania.databinding.FragmentMarketsBinding
import com.thecode.cryptomania.presentation.main.home.CoinItemUiModel
import com.thecode.cryptomania.utils.AppConstants.DEFAULT_CURRENCY
import com.thecode.cryptomania.utils.NavigationManager
import com.thecode.cryptomania.utils.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter


@AndroidEntryPoint
class MarketsFragment : Fragment() {

    // ViewModel
    private val viewModel: MarketViewModel by viewModels()

    // View Binding
    private var _binding: FragmentMarketsBinding? = null
    private val binding get() = _binding!!

    // Views
    private lateinit var marketRecyclerAdapter: MarketRecyclerViewAdapter
    private lateinit var exchangeRecyclerAdapter: ExchangeRecyclerViewAdapter


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
                    requireActivity(),
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
                requireActivity(),
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
            viewLifecycleOwner
        ) {
            when (it) {
                is DataState.Success -> {
                    hideBadStateLayout()
                    hideLoadingProgress()
                    showRecyclerViewMarket(true)
                    populateRecyclerViewMarket(it.data)
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
                NavigationManager().openCoinDetails(requireActivity(), it)
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
                NavigationManager().openExchangeDetailsActivity(requireActivity(), it)
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

    private fun populateRecyclerViewMarket(coins: List<CoinItemUiModel>) {
        if (coins.isEmpty()) {
            showBadStateLayout()
        } else {
            marketRecyclerAdapter.setCoinListItems(coins)
            binding.recyclerviewCryptoMarkets.scheduleLayoutAnimation()
        }
    }

    private fun populateRecyclerViewExchange(exchanges: List<ExchangeItemDomainModel>) {
        if (exchanges.isEmpty()) {
            showBadStateLayout()
        } else {
            exchangeRecyclerAdapter.setExchangeListItems(exchanges)
            binding.recyclerviewCryptoMarkets.scheduleLayoutAnimation()
        }
    }

    companion object {
        const val COIN_UI_MODEL = "coinUiModel"
    }

}
