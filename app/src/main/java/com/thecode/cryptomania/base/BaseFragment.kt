package com.thecode.cryptomania.base

import android.content.Intent
import androidx.fragment.app.Fragment
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType
import com.thecode.cryptomania.R
import com.thecode.cryptomania.core.domain.CoinItem
import com.thecode.cryptomania.presentation.coindetails.CoinDetailsActivity

open class BaseFragment : Fragment() {

    fun showErrorDialog(title: String, description: String) {
        AestheticDialog.Builder(requireActivity(), DialogStyle.RAINBOW, DialogType.ERROR)
            .setTitle(title)
            .setMessage(description)
            .setDuration(2000)
            .show()
    }

    fun showSuccessDialog(title: String, description: String) {
        AestheticDialog.Builder(requireActivity(), DialogStyle.RAINBOW, DialogType.SUCCESS)
            .setTitle(title)
            .setMessage(description)
            .setDuration(2000)
            .show()
    }

    fun openDetailsActivity(coin: CoinItem) {

        val i = Intent(context, CoinDetailsActivity::class.java)

        // ADD DATA TO OUR INTENT
        i.putExtra("title", coin.id)
        i.putExtra("description", coin.name)
        i.putExtra("imageUrl", coin.image)

        // START DETAIL ACTIVITY
        requireActivity().startActivity(i)
    }
}
