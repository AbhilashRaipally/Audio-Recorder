package com.ap.audiorecorder

import android.util.Log
import androidx.appcompat.widget.TooltipCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ap.tooltip.Tooltip

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
        control = recorderState.control
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
    control: AudioRecorderControl
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MicView(modifier, control)
    }
}

@Composable
private fun MicView(
    modifier: Modifier,
    control: AudioRecorderControl
) {
    var longPressActive by remember { mutableStateOf(false) }
    Spacer(modifier = modifier.height(24.dp))

    Box {
        val showTooltip = remember { mutableStateOf(false) }
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    color = if (longPressActive) Color(0xff00897B).copy(alpha = 0.2f) else
                        Color(0xff00897B)
                )
                .padding(all = 12.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            longPressActive = true
                            control.start()
                        },
                        onPress = {
                            awaitRelease()
                            showTooltip.value = !longPressActive
                            longPressActive = false
                            control.stop()
                        }

                    )
                },
            contentAlignment = Alignment.Center
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_mic_24),
                contentDescription = "record",
                tint = Color.White,
                modifier = modifier.fillMaxSize()
            )
        }

        Tooltip(
            visibility = showTooltip
        ) {
            // Tooltip content goes here.
            Text(
                text = "Hold to record, release to save",
                fontSize = 12.sp
            )
        }
    }
}

interface AudioRecorderControl {
    fun start()
    fun stop()
}

@Composable
@Preview(showBackground = true)
private fun Preview() {
    val control = object : AudioRecorderControl {
        override fun start() {}
        override fun stop() {}
    }
    Surface {
        AudioRecorderView(
            control = control
        )
    }
}