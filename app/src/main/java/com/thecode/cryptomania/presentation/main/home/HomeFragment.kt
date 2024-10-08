package com.thecode.cryptomania.presentation.main.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.databinding.FragmentHomeBinding
import com.thecode.cryptomania.utils.AppConstants.DEFAULT_CURRENCY
import com.thecode.cryptomania.utils.NavigationManager
import com.thecode.cryptomania.utils.showErrorDialog
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInLeftAnimationAdapter


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var coinCardRecyclerAdapter: CoinCardRecyclerViewAdapter
    private lateinit var rankingRecyclerAdapter: RankingRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        subscribeObservers()
        initViews()
        initRecyclerViews()
        fetchTopCryptoCurrency()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTopCryptoCurrency() {
        viewModel.getCoins(DEFAULT_CURRENCY)
    }

    private fun showInternetConnectionErrorLayout() {
        binding.apply {
            if (coinCardRecyclerAdapter.itemCount > 0) {
                showErrorDialog(
                    requireActivity(),
                    getString(R.string.network_error),
                    getString(R.string.check_internet)
                )
            } else {
                layoutContent.isVisible = false
                included.apply {
                    layoutBadState.isVisible = true
                    textState.text = getString(R.string.internet_connection_error)
                    btnRetry.isVisible = true
                }
            }
        }
    }

    private fun showBadStateLayout() {
        binding.apply {
            if (coinCardRecyclerAdapter.itemCount > 0) {
                showErrorDialog(
                    requireActivity(),
                    getString(R.string.error),
                    getString(R.string.service_unavailable)
                )
            } else {
                layoutContent.isVisible = false
                included.apply {
                    layoutBadState.isVisible = true
                    textState.text = getString(R.string.no_result_found)
                    btnRetry.isVisible = true
                }
            }
        }
    }

    private fun hideBadStateLayout() {
        binding.apply {
            included.layoutBadState.isVisible = false
            layoutContent.isVisible = true
        }
    }

    private fun subscribeObservers() {
        viewModel.coinState.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                is DataState.Success -> {
                    hideBadStateLayout()
                    hideLoadingProgress()
                    populateRecyclerView(it.data)
                }

                is DataState.Loading -> {
                    showLoadingProgress()
                }

                is DataState.Error -> {
                    hideLoadingProgress()
                    showInternetConnectionErrorLayout()
                }
            }
        }
    }

    private fun hideLoadingProgress() {
        binding.refreshLayout.isRefreshing = false
    }

    private fun showLoadingProgress() {
        binding.refreshLayout.isRefreshing = true
    }

    private fun initRecyclerViews() {
        coinCardRecyclerAdapter = CoinCardRecyclerViewAdapter(onOpenCoinDetails = {
            openCoinDetails(requireActivity(), it)
        })

        rankingRecyclerAdapter = RankingRecyclerViewAdapter(onOpenCoinDetails = {
            openCoinDetails(requireActivity(), it)
        })

        binding.apply {
            recyclerviewTopCrypto.apply {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                adapter = SlideInBottomAnimationAdapter(coinCardRecyclerAdapter)
            }

            recyclerviewCryptoRanking.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = SlideInLeftAnimationAdapter(rankingRecyclerAdapter)
            }
        }
    }

    private fun initViews() {
        binding.apply {
            included.btnRetry.setOnClickListener { fetchTopCryptoCurrency() }
            refreshLayout.setOnRefreshListener { fetchTopCryptoCurrency() }
            themedButtonGroup.apply {
                selectButton(btnHot)
                setOnSelectListener {
                    when (it) {
                        btnHot -> fetchTopCryptoCurrency()
                        btnWinners -> fetchTopCryptoCurrency()
                        btnLosers -> fetchTopCryptoCurrency()
                    }
                }
            }
        }
    }

    private fun populateRecyclerView(coins: List<CoinItemUiModel>) {
        binding.apply {
            if (coins.isEmpty()) {
                showBadStateLayout()
            } else {
                coinCardRecyclerAdapter.setCoinListItems(coins)
                recyclerviewTopCrypto.scheduleLayoutAnimation()

                when {
                    btnHot.isSelected -> {
                        rankingRecyclerAdapter.setCoinListItems(coins)
                    }

                    btnLosers.isSelected -> {
                        val coinList = coins.sortedBy { it.priceChangePercentage24h }
                        rankingRecyclerAdapter.setCoinListItems(coinList)
                    }

                    btnWinners.isSelected -> {
                        val coinList = coins.sortedByDescending { it.priceChangePercentage24h }
                        rankingRecyclerAdapter.setCoinListItems(coinList)
                    }
                }
                recyclerviewCryptoRanking.scheduleLayoutAnimation()
            }
        }
    }

    private fun openCoinDetails(context: Context, coin: CoinItemUiModel) {
        NavigationManager().openCoinDetails(context, coin)
    }
}