package com.thecode.cryptomania.presentation.main.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.thecode.cryptomania.R
import com.thecode.cryptomania.base.BaseFragment
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.core.domain.DataState
import com.thecode.cryptomania.databinding.FragmentHomeBinding
import com.thecode.cryptomania.presentation.coindetails.CoinDetailsActivity
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter
import jp.wasabeef.recyclerview.adapters.SlideInLeftAnimationAdapter
import nl.bryanderidder.themedtogglebuttongroup.ThemedButton
import nl.bryanderidder.themedtogglebuttongroup.ThemedToggleButtonGroup

@AndroidEntryPoint
class HomeFragment : BaseFragment(), CoinCardOnClickListener {

    // ViewModel
    private val viewModel: HomeViewModel by viewModels()

    // Listener
    private var coinOnClickListener: CoinCardOnClickListener = this

    // View Binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var category: String = "General"

    // Views
    private lateinit var recyclerViewTopCrypto: RecyclerView
    lateinit var recyclerViewRanking: RecyclerView
    lateinit var coinCardRecyclerAdapter: CoinCardRecyclerViewAdapter
    lateinit var rankingRecyclerAdapter: RankingRecyclerViewAdapter
    private lateinit var refreshLayout: SwipeRefreshLayout
    lateinit var btnRetry: AppCompatButton
    lateinit var layoutBadState: View
    lateinit var textState: TextView
    lateinit var imgState: ImageView
    private lateinit var themedButtonGroup: ThemedToggleButtonGroup
    private lateinit var btnHot: ThemedButton
    private lateinit var btnWinner: ThemedButton
    private lateinit var btnLoser: ThemedButton

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
        recyclerViewTopCrypto.scheduleLayoutAnimation()
        recyclerViewRanking.scheduleLayoutAnimation()
    }

    private fun showInternetConnectionErrorLayout() {
        if (coinCardRecyclerAdapter.itemCount > 0) {
            showErrorDialog(
                getString(R.string.network_error),
                getString(R.string.check_internet)
            )
        } else {
            layoutBadState.isVisible = true
            textState.text = getString(R.string.internet_connection_error)
            btnRetry.isVisible = true
        }
    }

    private fun showBadStateLayout() {
        if (coinCardRecyclerAdapter.itemCount > 0) {
            showErrorDialog(
                getString(R.string.error),
                getString(R.string.service_unavailable)
            )
        } else {
            layoutBadState.isVisible = true
            textState.text = getString(R.string.no_result_found)
            btnRetry.isVisible = true
        }
    }

    private fun hideBadStateLayout() {
        layoutBadState.isVisible = false
    }

    private fun subscribeObservers() {
        viewModel.coinState.observe(
            viewLifecycleOwner,
            {
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
        refreshLayout.isRefreshing = false
    }

    private fun showLoadingProgress() {
        refreshLayout.isRefreshing = true
    }

    private fun initRecyclerViews() {
        coinCardRecyclerAdapter = CoinCardRecyclerViewAdapter(coinOnClickListener)
        recyclerViewTopCrypto.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewTopCrypto.adapter = SlideInBottomAnimationAdapter(coinCardRecyclerAdapter)

        rankingRecyclerAdapter = RankingRecyclerViewAdapter(coinOnClickListener)
        recyclerViewRanking.layoutManager = LinearLayoutManager(activity)
        recyclerViewRanking.adapter = SlideInLeftAnimationAdapter(rankingRecyclerAdapter)
    }

    private fun initViews() {
        refreshLayout = binding.refreshLayout
        recyclerViewTopCrypto = binding.recyclerviewTopCrypto
        recyclerViewRanking = binding.recyclerviewCryptoRanking
        btnRetry = binding.included.btnRetry
        layoutBadState = binding.included.layoutBadState
        imgState = binding.included.imgState
        textState = binding.included.textState
        themedButtonGroup = binding.themedButtonGroup
        btnHot = binding.btnHot
        btnLoser = binding.btnLosers
        btnWinner = binding.btnWinners


        btnRetry.setOnClickListener { fetchTopCryptoCurrency() }

        refreshLayout.setOnRefreshListener {
            fetchTopCryptoCurrency()
        }

        themedButtonGroup.selectButton(btnHot)

        themedButtonGroup.setOnSelectListener {
            when (it) {
                btnHot -> {
                    category = "Hot"
                    showToast(category)
                    //fetchCryptoRanking()
                }

                btnWinner -> {
                    category = "Winner"
                    showToast(category)
                    //fetchCryptoRanking()
                }

                btnLoser -> {
                    category = "Loser"
                    showToast(category)
                    //fetchCryptoRanking()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private fun populateRecyclerView(coins: List<CoinItem>) {
        if (coins.isEmpty()) {
            showBadStateLayout()
        } else {
            val coinArrayList: ArrayList<CoinItem> = ArrayList()
            for (i in coins.indices) {
                val article = coins[i]
                coinArrayList.add(article)
                coinCardRecyclerAdapter.setCoinListItems(coinArrayList)
                recyclerViewTopCrypto.scheduleLayoutAnimation()

                rankingRecyclerAdapter.setCoinListItems(coinArrayList)
                recyclerViewRanking.scheduleLayoutAnimation()
            }
        }
    }


    override fun openCoinDetails(coin: CoinItem) {
        openCoinDetailsActivity(coin)
    }


}
