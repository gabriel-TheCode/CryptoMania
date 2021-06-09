package com.thecode.cryptomania.utils.extensions

import java.text.SimpleDateFormat
import java.util.*


fun String.toDate(): Date {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(this) ?: Date()
}

fun String.toDateHMS(): Date {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(this) ?: Date()
}
