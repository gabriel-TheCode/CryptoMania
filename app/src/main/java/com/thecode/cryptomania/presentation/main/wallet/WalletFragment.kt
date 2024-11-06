package com.thecode.cryptomania.presentation.main.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.thecode.cryptomania.databinding.FragmentWalletBinding

class WalletFragment : Fragment() {

    // ViewModel
    private val viewModel: WalletViewModel by viewModels()

    // View Binding
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Button click listeners for create and connect wallet
        binding.btnCreateWallet.setOnClickListener {
            viewModel.createWallet("user_password", requireContext().filesDir.absolutePath)
        }

        binding.btnConnectWallet.setOnClickListener {
            viewModel.connectWallet("user_password", "path_to_wallet_file")
        }

        // Observe the wallet creation and connection states
        observeViewModel()

        return binding.root

    }

    private fun observeViewModel() {
        viewModel.walletAddress.observe(viewLifecycleOwner) { address ->
            address?.let {
                Toast.makeText(requireContext(), "Wallet Address: $it", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}