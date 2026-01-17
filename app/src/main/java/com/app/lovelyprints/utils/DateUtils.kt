package com.app.lovelyprints.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatOrderDate(date: String?): String? {
    if (date.isNullOrBlank()) return null

    return try {

        // convert: 2026-02-01T14:32:11:23
        // into:   2026-02-01T14:32:11.023
        val normalized = date.replace(
            Regex(":(\\d{2})$"),
            ".0$1"
        )

        val input = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            Locale.getDefault()
        )

        val output = SimpleDateFormat(
            "dd MMM, hh:mm a",
            Locale.getDefault()
        )

        output.format(input.parse(normalized)!!)
    } catch (e: Exception) {
        null
    }
}


