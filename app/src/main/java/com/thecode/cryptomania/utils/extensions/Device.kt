package com.thecode.cryptomania.utils.extensions

import android.os.Build

fun supportsAndroid13(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
}