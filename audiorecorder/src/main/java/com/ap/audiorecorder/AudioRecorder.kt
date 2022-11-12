package com.ap.audiorecorder

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ap.tooltip.Tooltip

interface AudioRecorderControl {
    fun start()
    fun stop()
}

@Composable
fun AudioRecorder(
    recorderState: AudioRecorderState,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    if (!recorderState.isMicEnabled.value) {
        Log.d("AudioRecorder", "Device does not have mic")
        return
    }

    AudioRecorderView(
        modifier = modifier,
        state = recorderState
    )
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> recorderState.control.stop()
                else -> Unit
            }

        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            recorderState.control.stop()
        }
    }
}

@Composable
fun AudioRecorderView(
    modifier: Modifier = Modifier,
    state: AudioRecorderState
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.padding(5.dp)
            ) {
                if(state.isRecording.value){
                    MicLeftExpandedView(state = state)
                }
            }
            Box(
                Modifier.padding(5.dp)
                    .align(Alignment.CenterEnd),
            ) {
                MicView(modifier = modifier, state = state)
            }

        }
    }
}

@Composable
private fun MicView(
    modifier: Modifier,
    state: AudioRecorderState
) {
    var longPressActive by remember { mutableStateOf(false) }
    Spacer(modifier = modifier.height(24.dp))

    Box {
        val showTooltip = remember { mutableStateOf(false) }
        Tooltip(
            visibility = showTooltip
        ) {
            // Tooltip content goes here.
            Text(
                text = "Hold to record, release to save",
                fontSize = 12.sp
            )
        }

        Box(
            modifier = modifier
                .scale(if(longPressActive) 1.75F else 1F)
                .size(48.dp)
                .clip(CircleShape)
                .background(color = Color(0xff00897B))
                .padding(all = 12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            longPressActive = true
                            state.control.start()
                        },
                        onPress = {
                            awaitRelease()
                            showTooltip.value = !longPressActive
                            longPressActive = false
                            state.control.stop()
                        }

                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = R.drawable.ic_mic_24),
                contentDescription = "record",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun MicLeftExpandedView(
    modifier: Modifier = Modifier,
    state: AudioRecorderState
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colors.surface,
        border = BorderStroke(2.dp, MaterialTheme.colors.surface),
        elevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(2.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = modifier.padding(5.dp)) {
                Blinking { alpha ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mic_24),
                        tint = Color.Red,
                        contentDescription = "mic",
                        modifier = Modifier.alpha(alpha = alpha)
                    )
                }
            }

            Row(
                modifier = modifier.padding(5.dp)
            ) {
                Text(
                    text = state.recordDuration.value,
                )
            }

        }
    }
}