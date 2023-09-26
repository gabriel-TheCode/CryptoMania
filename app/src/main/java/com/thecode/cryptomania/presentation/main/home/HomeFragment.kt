package com.thecode.cryptomania.presentation.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.thecode.cryptomania.R
import com.thecode.cryptomania.base.BaseFragment
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInLeftAnimationAdapter

@AndroidEntryPoint
class HomeFragment : BaseFragment(), CoinCardOnClickListener {

    private val viewModel: HomeViewModel by viewModels()

    private var coinOnClickListener: CoinCardOnClickListener = this

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var coinCardRecyclerAdapter: CoinCardRecyclerViewAdapter
    private lateinit var rankingRecyclerAdapter: RankingRecyclerViewAdapter
    private lateinit var coinList: List<CoinItem>
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        subscribeObservers()
        initViews()
        initRecyclerViews()
        fetchTopCryptoCurrency()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchTopCryptoCurrency() {
        viewModel.getCoins("usd")
    }

    private fun showInternetConnectionErrorLayout() {
        binding.apply {
            if (coinCardRecyclerAdapter.itemCount > 0) {
                showErrorDialog(
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
                    populateRecyclerView(it.data.coins)
                }

                is DataState.Loading -> {
                    showLoadingProgress()
                }

                is DataState.Error -> {
                    hideLoadingProgress()
                    showInternetConnectionErrorLayout()
                    showToast(getString(R.string.internet_connection_error))
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
        coinCardRecyclerAdapter = CoinCardRecyclerViewAdapter(coinOnClickListener)

        binding.apply {
            recyclerviewTopCrypto.apply {
                layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
                adapter = SlideInBottomAnimationAdapter(coinCardRecyclerAdapter)
            }
            rankingRecyclerAdapter = RankingRecyclerViewAdapter(coinOnClickListener)
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

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private fun populateRecyclerView(coins: List<CoinItem>) {
        binding.apply {
            if (coins.isEmpty()) {
                showBadStateLayout()
            } else {
                val coinArrayList: ArrayList<CoinItem> = ArrayList()
                for (i in coins.indices) {
                    val article = coins[i]
                    coinArrayList.add(article)
                }
                coinCardRecyclerAdapter.setCoinListItems(coinArrayList)
                recyclerviewTopCrypto.scheduleLayoutAnimation()

                when {
                    btnHot.isSelected -> {
                        rankingRecyclerAdapter.setCoinListItems(coinArrayList)
                    }

                    btnLosers.isSelected -> {
                        coinList = coins.sortedBy { it.price_change_percentage_24h }
                        rankingRecyclerAdapter.setCoinListItems(coinList)
                    }

                    btnWinners.isSelected -> {
                        coinList = coins.sortedByDescending { it.price_change_percentage_24h }
                        rankingRecyclerAdapter.setCoinListItems(coinList)
                    }
                }
                recyclerviewCryptoRanking.scheduleLayoutAnimation()
            }
        }
    }

    override fun openCoinDetails(coin: CoinItem) {
        openCoinDetailsActivity(coin)
    }
}
