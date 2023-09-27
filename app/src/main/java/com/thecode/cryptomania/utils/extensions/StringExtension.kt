package com.thecode.cryptomania.utils.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun String.toDate(): Date {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(this) ?: Date()
}

fun String.toDateYMD(): Date {
    val pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    return SimpleDateFormat(pattern, Locale.getDefault()).parse(this) ?: Date()
}

fun String.addSuffix(value: String): String {
    return this + value
}

fun String.addPrefix(value: String): String {
    return value + this
}

