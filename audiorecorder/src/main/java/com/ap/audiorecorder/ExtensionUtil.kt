package com.ap.audiorecorder

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import java.util.*

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Long.formatMillis(): String {
    val formatBuilder = StringBuilder()
    val formatter = Formatter(formatBuilder, Locale.getDefault())
    getStringForTime(formatBuilder, formatter, this)
    return formatBuilder.toString()
}

/**
 * Returns the specified millisecond time formatted as a string.
 *
 * @param builder The builder that `formatter` will write to.
 * @param formatter The formatter.
 * @param timeMs The time to format as a string, in milliseconds.
 * @return The time formatted as a string.
 */
fun getStringForTime(builder: StringBuilder, formatter: Formatter, timeMs: Long): String {
    val totalSeconds = (timeMs + 500) / 1000
    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600
    builder.setLength(0)
    return if (hours > 0)
        formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    else
        formatter.format("%02d:%02d", minutes, seconds).toString()
}