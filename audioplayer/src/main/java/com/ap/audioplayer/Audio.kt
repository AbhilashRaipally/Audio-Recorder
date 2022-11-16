package com.ap.audioplayer

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Audio(
    val description: String,
    val url: String,
    val subtitle: String,
    val title: String,
    val thumb: String
): Parcelable

@Parcelize
data class AudioList(
    val audios: List<Audio>
): Parcelable