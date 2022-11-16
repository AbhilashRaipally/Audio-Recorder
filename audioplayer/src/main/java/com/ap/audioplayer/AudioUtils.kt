@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalTime::class)

package com.ap.audioplayer

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime


/**
 * Will return a timestamp denoting the current audio [duration] in the following
 * format "mm:ss / mm:ss"
 * **/
internal inline fun prettyAudioTimestamp(
    duration: Duration
): String = buildString {
    appendMinutesAndSeconds(duration)
}

/**
 * Will split [duration] in minutes and seconds and append it to [this] in the following format "mm:ss"
 * */
private fun StringBuilder.appendMinutesAndSeconds(duration: Duration) {
    val minutes = duration.inWholeMinutes
    val seconds = (duration - minutes.minutes).inWholeSeconds
    appendDoubleDigit(minutes)
    append(':')
    appendDoubleDigit(seconds)
}

/**
 * Will append [value] as double digit to [this].
 * If a single digit value is passed, ex: 4 then a 0 will be added as prefix resulting in 04
 * */
private fun StringBuilder.appendDoubleDigit(value: Long) {
    if (value < 10) {
        append(0)
        append(value)
    } else {
        append(value)
    }
}

enum class AudioType {
    MP3, HLS
}

internal inline fun Uri.getMediaType(): AudioType {
    return when (Util.inferContentType(this)) {
        C.CONTENT_TYPE_HLS -> AudioType.HLS
        else -> AudioType.MP3
    }
}

internal inline fun Uri.getMediaSource(
    context: Context,
): MediaSource {

    val mediaType = getMediaType()
    val mediaItem = MediaItem.fromUri(this)

    return when (mediaType) {
        AudioType.HLS -> HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(mediaItem)
        else ->
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
                .createMediaSource(mediaItem)
    }
}

/**
 * Returns last part of the uri after "/" without file extension.
 * If there's no last part or no file extension it returns "No name"
 */
internal fun Uri.getFileName(): String {
    val path = this.path
    val lastPartWithExtension = path?.substring(path.lastIndexOf('/') + 1)
    return lastPartWithExtension?.substringBeforeLast('.', lastPartWithExtension) ?: "No name"
}