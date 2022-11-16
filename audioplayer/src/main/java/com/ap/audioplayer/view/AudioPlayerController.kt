package com.ap.audioplayer.view

import android.annotation.SuppressLint
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ap.audioplayer.AudioPlayerState
import com.ap.audioplayer.R
import com.ap.audioplayer.findActivity
import com.ap.audioplayer.prettyAudioTimestamp
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun AudioPlayerController(
    state: AudioPlayerState,
    title: String,
    subtitle: String? = null,
    backgroundColor: Color = Color.Transparent,
    contentColor: Color = Color.White,
) {
    val context = LocalContext.current
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .height(70.dp)
                .background(color = backgroundColor),
            ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlaybackControl(
                        isPlaying = state.isPlaying.value,
                        control = state.control
                    )
                    TimeLineControl(
                        modifier = Modifier.fillMaxWidth(),
                        audioDurationMs = state.audioDurationMs.value,
                        audioPositionMs = state.audioPositionMs.value,
                        onValueChange = { state.dragAudioScreen(it) },
                        onValueChangeFinished = {
                            state.dragAudioScreenFinish(state.audioProgress.value)
                        },
                        audioPlayerState = state
                    )
                }

            }
        }

    }
}

@Composable
private fun ControlHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String?,
    onBackClick: (() -> Unit)?,
    settingsContent: @Composable (() -> Unit) -> Unit,
    onDownloadClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdaptiveIconButton(onClick = { onBackClick?.invoke() }) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
        }

        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = LocalContentColor.current,
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = LocalContentColor.current.copy(0.80f),
                    style = MaterialTheme.typography.body1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

    }
}

@Composable
private fun PlaybackControl(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    control: AudioPlayerControl
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (isPlaying) control.pause() else control.play() }
        ) {
            val resId = if (isPlaying) R.drawable.ic_baseline_pause_24 else R.drawable.ic_baseline_play_arrow_24
            Icon(
                painter = painterResource(id = resId),
                contentDescription = null,
                tint = Color.Unspecified
            )
        }
    }
}


@Composable
fun TimeLineControl(
    modifier: Modifier,
    audioDurationMs: Long,
    audioPositionMs: Long,
    onValueChange: (value: Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    audioPlayerState: AudioPlayerState,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = prettyAudioTimestamp(audioPositionMs.milliseconds))
            Spacer(modifier = Modifier.width(6.dp))
            Slider(
                modifier = Modifier
                    .weight(1F)
                    .height(2.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.LightGray,
                    inactiveTickColor = Color.LightGray,
                    activeTrackColor = Color.LightGray,
                    activeTickColor = Color.LightGray,
                ),
                value = audioPlayerState.audioProgress.value,
                //valueRange = 0f.. (audioPlayerState.audioDurationMs.value + 0.0f),
                onValueChange = onValueChange,
                onValueChangeFinished = onValueChangeFinished
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = prettyAudioTimestamp(audioDurationMs.milliseconds))
        }
    }
}

/**
 * Allow the button to be any size instead of constraining it to 48dp
 * **/
@Composable
fun AdaptiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = content)
    }
}

private val BigIconButtonSize = 48.dp
private val SmallIconButtonSize = 32.dp


@Preview(showBackground = true)
@Composable
fun PlaybackControllerPreview() {
    val control = object : AudioPlayerControl {
        override fun play() {}
        override fun pause() {}
        override fun forward() {}
        override fun rewind() {}
    }
    PlaybackControl(isPlaying = true, control = control)
}
