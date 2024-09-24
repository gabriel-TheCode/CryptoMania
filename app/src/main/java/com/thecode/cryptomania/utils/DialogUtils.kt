package com.thecode.cryptomania.utils

import android.app.Activity
import com.thecode.aestheticdialogs.AestheticDialog
import com.thecode.aestheticdialogs.DialogStyle
import com.thecode.aestheticdialogs.DialogType


fun showErrorDialog(activity: Activity, title: String, description: String) {
    AestheticDialog.Builder(activity, DialogStyle.RAINBOW, DialogType.ERROR)
        .setTitle(title)
        .setMessage(description)
        .setDuration(2000)
        .show()

}
