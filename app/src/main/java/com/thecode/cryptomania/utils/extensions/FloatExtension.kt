package com.thecode.cryptomania.utils.extensions

import kotlin.math.ln
import kotlin.math.pow


fun Float.withNumberSuffix(): String {
    if (this < 1000) return this.toString()

    val suffixes = "kMBTPE"
    val exp = (ln(this.toDouble()) / ln(1000.0)).toInt()

    // Limit the suffix to the available range
    val suffixIndex = (exp - 1).coerceIn(0, suffixes.lastIndex)

    val value = this / 1000.0.pow(exp.toDouble())
    return String.format("%.1f %c", value, suffixes[suffixIndex])
}