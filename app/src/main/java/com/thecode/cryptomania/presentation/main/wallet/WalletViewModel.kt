package com.thecode.cryptomania.presentation.main.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.crypto.WalletUtils
import java.io.File
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor() : ViewModel() {

    private val _walletAddress = MutableLiveData<String?>()
    val walletAddress: LiveData<String?> get() = _walletAddress

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Creates a new wallet and sets the wallet address
    fun createWallet(password: String, fileDirectory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletFile =
                    WalletUtils.generateNewWalletFile(password, File(fileDirectory), false)
                val credentials =
                    WalletUtils.loadCredentials(password, File(fileDirectory, walletFile))
                _walletAddress.postValue(credentials.address)
            } catch (e: Exception) {
                _error.postValue("Failed to create wallet: ${e.localizedMessage}")
            }
        }
    }

    // Connects to an existing wallet and sets the wallet address
    fun connectWallet(password: String, walletFilePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val credentials = WalletUtils.loadCredentials(password, File(walletFilePath))
                _walletAddress.postValue(credentials.address)
            } catch (e: Exception) {
                _error.postValue("Failed to load wallet: ${e.localizedMessage}")
            }
        }
    }
}
