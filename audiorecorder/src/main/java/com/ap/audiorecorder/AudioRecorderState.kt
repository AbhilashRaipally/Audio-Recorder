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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

interface AudioRecorderState {
    val recorder: MediaRecorder
    val recordFilePath: State<String>
    val recordDuration: State<String>
    val isRecording: State<Boolean>
    val isRecordingSaved: State<Boolean>
    val recordedFileUrl: State<String>
    val control: AudioRecorderControl
    val isMicEnabled: State<Boolean>

    fun setFilePath(path: String)
    fun deleteRecording()
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

    override val recordDuration = mutableStateOf("")
    override val isRecording = mutableStateOf(false)
    override val isRecordingSaved = mutableStateOf(false)
    override val recordedFileUrl = mutableStateOf("")

    private var startTime: Long = 0L
    private var recordStatsJob: Job? = null
    private var timer = Timer()

    override val control: AudioRecorderControl = object : AudioRecorderControl {
        override fun start() {
            val now = Calendar.getInstance().time
            val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
            val recordFileName = formatter.format(now)
            val currentActivity = context.findActivity() ?: return

            if (!isAudioRecordPermissionGranted(currentActivity)) {
                if (shouldShowRequestPermissionRationale(
                        currentActivity,
                        Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    Toast.makeText(
                        currentActivity,
                        "App requires access to audio",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                ActivityCompat.requestPermissions(
                    currentActivity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    99
                )
                return
            }

            if (!isMicEnabled.value) {
                Toast.makeText(
                    currentActivity,
                    "Device does not have mic to record audio",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            recordedFileUrl.value = "${recordFilePath.value}${File.separator}${recordFileName}.mp3"

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setAudioSamplingRate(16000)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder.setOutputFile(recordedFileUrl.value)
            recorder.prepare()
            recorder.start()
            onRecordingStarted()

        }

        override fun stop() {
            if (isRecording.value) {
                recorder.stop()
                recorder.reset()
            }
            onRecordingStopped()
        }

    }

    private fun onRecordingStopped() {
        if (isRecording.value) {
            isRecording.value = false
            isRecordingSaved.value = true
        }
        resetRecordStartTime()
        recordStatsJob?.cancel()
        recordDuration.value = ""
        timer.cancel()
    }

    private fun onRecordingStarted() {
        isRecording.value = true
        isRecordingSaved.value = false
        setRecordStartTime()
        timer = Timer()
        val timerTask = timerTask {
            updateRecordDuration()
        }
        timer.scheduleAtFixedRate(timerTask, 0, 1000)

    }

    override fun setFilePath(path: String) {
        recordFilePath.value = path
    }

    override fun deleteRecording() {
        try {
            val file = File(recordedFileUrl.value)
            val result = file.delete()
            if (!result) {
                Log.d(TAG, "Deletion failed.")
            }
        } catch (e: Exception) {
            Log.d(TAG, "Deletion failed.+$e")
        }
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

    private fun setRecordStartTime() {
        startTime = System.currentTimeMillis()
    }

    private fun resetRecordStartTime() {
        startTime = 0
    }

    private fun updateRecordDuration() {
        recordDuration.value = (System.currentTimeMillis() - startTime).formatMillis()
    }
}
