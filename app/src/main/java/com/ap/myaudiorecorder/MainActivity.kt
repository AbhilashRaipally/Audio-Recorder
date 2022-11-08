package com.ap.myaudiorecorder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ap.audiorecorder.AudioRecorder
import com.ap.audiorecorder.rememberAudioRecorderState
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
                    Recorder()
                }
            }
        }
    }
}

@Composable
private fun Recorder() {
    val recorderState = rememberAudioRecorderState()
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            AudioRecorder(
                recorderState = recorderState,
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            Box() {
                Text(
                    text = recorderState.recordDuration.value,

                )
            }

        }
    }


}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyAudioRecorderTheme {
        Recorder()
    }
}