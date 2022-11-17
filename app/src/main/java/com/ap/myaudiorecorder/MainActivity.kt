package com.ap.myaudiorecorder

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.ap.audioplayer.Audio
import com.ap.audioplayer.rememberAudioPlayerState
import com.ap.audioplayer.view.AudioPlayer
import com.ap.audioplayer.view.AudioPlayerController
import com.ap.audiorecorder.AudioRecorder
import com.ap.audiorecorder.AudioRecorderState
import com.ap.audiorecorder.rememberAudioRecorderState
import com.ap.audiorecorder.swipeToDismiss
import com.ap.myaudiorecorder.ui.theme.MyAudioRecorderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAudioRecorderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val recorderState = rememberAudioRecorderState()

                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1F)
                        ) {
                            if (recorderState.isRecordingSaved.value) {
                                val audioData = Audio(
                                    title = "Recorded Audio",
                                    url = recorderState.recordedFileUrl.value,
                                    description = "",
                                    subtitle = "",
                                    thumb = ""
                                )
                                Box(
                                    modifier = Modifier
                                        .swipeToDismiss { recorderState.deleteRecording() }
                                ) {
                                    Player(audioData)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Recorder(recorderState)
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun Recorder(recorderState: AudioRecorderState) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AudioRecorder(
                recorderState = recorderState,
            )
        }
    }
}

@Composable
private fun Player(
    data: Audio
) {
    val playerState = rememberAudioPlayerState()

    val context = LocalContext.current
    LaunchedEffect(data) {
        playerState.setSource(data, context)
    }
    AudioPlayer(
        modifier = Modifier
            .fillMaxWidth(),
        playerState = playerState,
    ) {
        AudioPlayerController(
            state = playerState,
            title = data.title
        )
    }
}