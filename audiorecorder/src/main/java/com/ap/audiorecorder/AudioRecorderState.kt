package com.ap.audiorecorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import kotlinx.coroutines.CoroutineScope

interface AudioRecorderState {
    val recorder: MediaRecorder

    val recordFilePath: State<String>
    val audioSource: State<Int>
    val outputFormat: State<Int>
    val audioEncoder: State<Int>

    @OptIn(ExperimentalPermissionsApi::class)
    val permissionsState: State<MultiplePermissionsState>
    val recordProgress: State<Float>

    val isRecording: State<Boolean>

    val control: AudioRecorderControl

}

@Composable
fun rememberAudioRecorderState(
    key: Any? = null,
    recorder: MediaRecorder? = null,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    config: MediaRecorder.() -> Unit = {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
    },
): AudioRecorderState = remember(key) {
    AudioRecorderStateImpl(
        recorder = recorder ?: getMediaRecorder(context).apply(config),
        coroutineScope = coroutineScope,
    ).also {
        it.recorder.setOnErrorListener(it)
        it.recorder.setOnInfoListener(it)
    }
}

private fun getMediaRecorder(context: Context): MediaRecorder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        MediaRecorder()
    }
}

class AudioRecorderStateImpl(
    override val recorder: MediaRecorder,
    private val coroutineScope: CoroutineScope,
) : AudioRecorderState, MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {
    override val recordFilePath: State<String>
        get() = TODO("Not yet implemented")
    override val audioSource: State<Int>
        get() = TODO("Not yet implemented")
    override val outputFormat: State<Int>
        get() = TODO("Not yet implemented")
    override val audioEncoder: State<Int>
        get() = TODO("Not yet implemented")

    @OptIn(ExperimentalPermissionsApi::class)
    override val permissionsState: State<MultiplePermissionsState>
        get() = TODO("Not yet implemented")
    override val recordProgress: State<Float>
        get() = TODO("Not yet implemented")
    override val isRecording: State<Boolean>
        get() = TODO("Not yet implemented")
    override val control: AudioRecorderControl
        get() = TODO("Not yet implemented")

    override fun onError(p0: MediaRecorder?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun onInfo(p0: MediaRecorder?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

}
