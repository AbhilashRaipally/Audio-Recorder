package com.ap.audiorecorder

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun AudioRecorder(
    recorderState: AudioRecorderState,
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    if(!recorderState.isMicEnabled.value){
        Log.d("AudioRecorder","Device does not have mic")
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
    var longPressActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = modifier.height(24.dp))
        Box(
            modifier = modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    color = if (longPressActive) MaterialTheme.colors.error.copy(alpha = 0.2f) else
                        MaterialTheme.colors.error
                )
                .padding(all = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            longPressActive = true
                            control.start()
                        },
                        onPress = {
                            awaitRelease()
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
                tint = MaterialTheme.colors.surface,
                modifier = modifier.fillMaxSize()
            )
        }
        Spacer(modifier = modifier.height(8.dp))
        Text(text = "Hold and record")
        Spacer(modifier = modifier.height(24.dp))
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