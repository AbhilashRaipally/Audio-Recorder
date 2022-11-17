package com.ap.audioplayer.view

import android.content.Context
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.ap.audioplayer.AudioPlayerState
import com.ap.audioplayer.AudioPlayerStateImpl
import com.google.android.exoplayer2.ExoPlayer

/**
 * @param playerState state to attach to this composable.
 * @param lifecycleOwner required to manage the ExoPlayer instance.
 * @param controller you can use [AudioPlayerControl] or alternatively implement your own
 * */
@Composable
fun AudioPlayer(
    playerState: AudioPlayerState,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    controller: @Composable () -> Unit,
) {
    AudioPlayerView(
        modifier = modifier,
        playerState = playerState,
        controller = controller
    )
    DisposableEffect(lifecycleOwner) {
        onDispose {
            playerState.player.release()
        }
    }
}


@Composable
private fun AudioPlayerView(
    modifier: Modifier,
    playerState: AudioPlayerState,
    controller: @Composable () -> Unit
) {
    var centerX by remember {
        mutableStateOf(0F)
    }
    Card(
        shape = RoundedCornerShape(15.dp),
        modifier = modifier.padding(8.dp),
            //.defaultPlayerTapGestures(playerState, centerX),
        backgroundColor = Color(0xff00897B)
    ) {
        controller()
    }
}

private fun Modifier.defaultPlayerTapGestures(playerState: AudioPlayerState, centerX: Float) =
    pointerInput(centerX) {
        detectTapGestures(
            onDoubleTap = {
                if (it.x > centerX) {
                    playerState.control.forward()
                } else {
                    playerState.control.rewind()
                }
            },
            onTap = {
                if (playerState.isPlaying.value) {
                    playerState.control.pause()
                } else {
                    playerState.control.play()
                }
            }
        )
    }

interface AudioPlayerControl {
    fun play()
    fun pause()

    fun forward()
    fun rewind()
}

@Preview
@Composable
private fun AudioPlayerPreview() {
    Surface() {
        val context: Context = LocalContext.current

        val player = ExoPlayer.Builder(context).build()

        val audioPositionPollInterval: Long = 500

        AudioPlayerView(
            modifier = Modifier,
            playerState = AudioPlayerStateImpl(
                coroutineScope = rememberCoroutineScope(),
                player = player,
                audioPositionPollInterval = audioPositionPollInterval,
            )
        ) {}
    }
}
