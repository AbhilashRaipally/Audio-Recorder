package com.ap.audiorecorder

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

interface AudioRecorderState {
    val recorder: MediaRecorder
    val recordFilePath: State<String>
    val recordDuration: State<String>
    val isRecording: State<Boolean>
    val control: AudioRecorderControl
    val isMicEnabled: State<Boolean>

    fun setFilePath(path: String)
}

@Composable
fun rememberAudioRecorderState(
    key: Any? = null,
    recorder: MediaRecorder? = null,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
): AudioRecorderState = remember(key) {
    AudioRecorderStateImpl(
        recorder = recorder ?: getMediaRecorder(context),
        coroutineScope = coroutineScope,
        context = context
    ).also {
        it.recorder.setOnErrorListener(it)
        it.recorder.setOnInfoListener(it)
    }
}

@Suppress("DEPRECATION")
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
    private val context: Context
) : AudioRecorderState, MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {

    companion object {
        private const val TAG = "AudioRecorderStateImpl"
    }

    override val recordFilePath = mutableStateOf("${context.externalCacheDir}")
    override val isMicEnabled = mutableStateOf(checkIsMicEnabled(context))

    override val recordDuration = mutableStateOf("0")
    override val isRecording = mutableStateOf(false)

    private var startTime: Long = 0L

    override val control: AudioRecorderControl = object : AudioRecorderControl {
        override fun start() {
            val now = Calendar.getInstance().time
            val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val recordFileName = formatter.format(now)
            val currentActivity = context.findActivity() ?: return

            if (!isAudioRecordPermissionGranted(currentActivity)) {
                if (shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(currentActivity, "App requires access to audio", Toast.LENGTH_SHORT).show()
                }
                ActivityCompat.requestPermissions(
                    currentActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    99
                )
                return
            }

            if(!isMicEnabled.value){
                Toast.makeText(currentActivity, "Device does not have mic to record audio", Toast.LENGTH_SHORT).show()
                return
            }

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setAudioSamplingRate(16000)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder.setOutputFile("${recordFilePath.value}${File.separator}${recordFileName}.3gp")
            recorder.prepare()
            recorder.start()
            isRecording.value = true
            setRecordStartTime()
        }

        override fun stop() {
            if (isRecording.value) {
                recorder.stop()
                isRecording.value = false
                recorder.reset()
            }
            resetRecordStartTime()
        }

    }

    override fun setFilePath(path: String) {
        recordFilePath.value = path
    }

    override fun onError(recorder: MediaRecorder?, what: Int, extra: Int) {
        when (what) {
            MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN -> {
                Log.e(TAG, "Recorder error unknown")
            }
            MediaRecorder.MEDIA_ERROR_SERVER_DIED -> {
                Log.e(TAG, "Recorder error server died")
            }
        }
    }

    override fun onInfo(recorder: MediaRecorder?, what: Int, extra: Int) {
        when (what) {
            MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN -> {
                Log.d(TAG, "Recorder info unknown")
            }
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                Log.w(TAG, "Recorder max duration reached")
            }
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                Log.w(TAG, "Recorder max file size reached")
            }
        }
    }

    private fun checkIsMicEnabled(context: Context): Boolean {
        val feature = PackageManager.FEATURE_MICROPHONE
        return context.packageManager.hasSystemFeature(feature)
    }

    private fun isAudioRecordPermissionGranted(currentActivity: Activity) =
        ContextCompat.checkSelfPermission(
            currentActivity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    private fun setRecordStartTime(){
        startTime = System.currentTimeMillis()
    }

    private fun resetRecordStartTime(){
        startTime = 0
    }
}
