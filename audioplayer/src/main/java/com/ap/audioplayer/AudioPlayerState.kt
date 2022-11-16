package com.ap.audioplayer

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.ap.audioplayer.view.AudioPlayerControl
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MUSIC
import com.google.android.exoplayer2.C.USAGE_MEDIA
import com.google.android.exoplayer2.audio.AudioAttributes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface AudioPlayerState {
    val player: ExoPlayer

    val audioPositionMs: State<Long>
    val audioDurationMs: State<Long>

    val audioProgressScrolling: State<Boolean>
    val audioProgress: State<Float>

    val dragAudioScreen: (dragProcess: Float) -> Unit
    val dragAudioScreenFinish: (endDragProcess: Float) -> Unit

    val isPlaying: State<Boolean>
    val playerState: State<Int>

    val control: AudioPlayerControl

    val currentAudioSource: State<Audio?>

    fun setSource(
        data: Audio,
        context: Context,
        playWhenReady: Boolean = false
    )
}

/**
 * Build and remember default implementation of [AudioPlayerState]
 *
 * @param audioPositionPollInterval interval on which the [AudioPlayerState.audioPositionMs] will be updated,
 * you can set a lower number to update the ui faster though it will consume more cpu resources.
 * @param coroutineScope this scope will be used to poll for [AudioPlayerState.audioPositionMs] updates
 * @param context used to build an [ExoPlayer] instance
 * @param config you can use this to configure [ExoPlayer]
 * */
@Composable
fun rememberAudioPlayerState(
    key: Any? = null,
    player: ExoPlayer? = null,
    audioPositionPollInterval: Long = 100,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    config: ExoPlayer.Builder.() -> Unit = {
        setSeekBackIncrementMs(10 * 1000)
        setSeekForwardIncrementMs(10 * 1000)
    },
): AudioPlayerState = remember(key) {
    val audioAttributes = AudioAttributes.Builder()
        .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(USAGE_MEDIA)
        .build()
    AudioPlayerStateImpl(
        player = player ?: ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .apply(config).build(),
        coroutineScope = coroutineScope,
        audioPositionPollInterval = audioPositionPollInterval,
    ).also {
        it.player.addListener(it)
    }
}

class AudioPlayerStateImpl(
    override val player: ExoPlayer,
    private val coroutineScope: CoroutineScope,
    private val audioPositionPollInterval: Long,
) : AudioPlayerState, Player.Listener {

    override val audioPositionMs = mutableStateOf(0L)
    override val audioDurationMs = mutableStateOf(0L)
    override val audioProgressScrolling = mutableStateOf(false)
    override val audioProgress = mutableStateOf(0F)
    override val currentAudioSource = mutableStateOf<Audio?>(null)

    override val dragAudioScreen: (Float) -> Unit
        get() = {
            audioProgressScrolling.value = true
            this.audioProgress.value = it
        }
    override val dragAudioScreenFinish: (endDragProcess: Float) -> Unit
        get() = {
            audioProgressScrolling.value = false

            player.seekTo((player.duration * audioProgress.value).toLong())
            if (player.playbackState == Player.STATE_IDLE) {
                player.prepare()
            }
        }
    override val isPlaying = mutableStateOf(player.isPlaying)
    override val playerState = mutableStateOf(player.playbackState)

    private var pollAudioPositionJob: Job? = null
    private var controlUiLastInteractionMs = 0L

    override fun setSource(
        data: Audio,
        context: Context,
        playWhenReady: Boolean
    ) {
        val uri = Uri.parse(data.url)
        val mediaSource = uri.getMediaSource(context)
        currentAudioSource.value = data

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = playWhenReady
    }

    override val control = object : AudioPlayerControl {
        override fun play() {
            controlUiLastInteractionMs = 0
            val state = player.playbackState
            if (state == Player.STATE_IDLE) {
                player.prepare()
            } else if (state == Player.STATE_ENDED) {
                player.seekTo(player.currentMediaItemIndex, C.TIME_UNSET)
            }
            player.play()

            pollAudioPositionJob = coroutineScope.launch {
                while (true) {
                    //(value * uiState.value.totalDuration).toLong()
                    audioPositionMs.value = player.currentPosition
                    audioProgress.value = (player.currentPosition + 0.0f) / player.duration
                    delay(audioPositionPollInterval)
                }
            }
        }

        override fun pause() {
            controlUiLastInteractionMs = 0
            player.pause()
        }

        override fun forward() {
            controlUiLastInteractionMs = 0
            player.seekForward()
        }

        override fun rewind() {
            controlUiLastInteractionMs = 0
            player.seekBack()
        }

    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        this.isPlaying.value = isPlaying
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) audioDurationMs.value = player.duration
        this.playerState.value = playbackState
    }
}