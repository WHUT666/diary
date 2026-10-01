package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class RecordingState(
    val isRecording: Boolean = false,
    val durationSeconds: Int = 0,
    val amplitude: Float = 0f,
    val filePath: String? = null
)

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val playingPath: String? = null
)

class AudioJournalManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingFile: File? = null

    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var recordingJob: Job? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val audioDir: File
        get() {
            val dir = File(context.filesDir, "audio_notes")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun startRecording(): Result<File> {
        return try {
            stopPlayback()

            val file = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            currentRecordingFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _recordingState.value = RecordingState(
                isRecording = true,
                durationSeconds = 0,
                amplitude = 0.1f,
                filePath = file.absolutePath
            )

            // Start polling timer and amplitude
            recordingJob?.cancel()
            recordingJob = scope.launch {
                var seconds = 0
                while (isActive && _recordingState.value.isRecording) {
                    delay(200)
                    val amp = try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        (maxAmp / 32767f).coerceIn(0.05f, 1f)
                    } catch (e: Exception) {
                        0.1f
                    }
                    val elapsedSec = (_recordingState.value.durationSeconds)
                    _recordingState.value = _recordingState.value.copy(
                        durationSeconds = elapsedSec,
                        amplitude = amp
                    )
                }
            }

            // Duration timer ticker
            scope.launch {
                var sec = 0
                while (isActive && _recordingState.value.isRecording) {
                    delay(1000)
                    sec++
                    _recordingState.value = _recordingState.value.copy(durationSeconds = sec)
                }
            }

            Result.success(file)
        } catch (e: Exception) {
            Log.e("AudioJournalManager", "Failed to start recording", e)
            releaseRecorder()
            Result.failure(e)
        }
    }

    fun stopRecording(): Pair<String, Int>? {
        return try {
            recordingJob?.cancel()
            val file = currentRecordingFile
            val duration = _recordingState.value.durationSeconds

            try {
                mediaRecorder?.stop()
            } catch (e: Exception) {
                Log.w("AudioJournalManager", "Stop recorder error", e)
            }
            releaseRecorder()

            _recordingState.value = RecordingState(isRecording = false)

            if (file != null && file.exists() && file.length() > 0) {
                Pair(file.absolutePath, duration.coerceAtLeast(1))
            } else {
                file?.delete()
                null
            }
        } catch (e: Exception) {
            Log.e("AudioJournalManager", "Failed to stop recording", e)
            releaseRecorder()
            _recordingState.value = RecordingState(isRecording = false)
            null
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            // ignore
        }
        releaseRecorder()
        currentRecordingFile?.delete()
        currentRecordingFile = null
        _recordingState.value = RecordingState(isRecording = false)
    }

    fun playAudio(filePath: String) {
        if (filePath.isBlank()) return
        val file = File(filePath)
        if (!file.exists()) return

        try {
            if (_playbackState.value.playingPath == filePath && _playbackState.value.isPlaying) {
                // Pause
                mediaPlayer?.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                playbackJob?.cancel()
                return
            } else if (_playbackState.value.playingPath == filePath && mediaPlayer != null) {
                // Resume
                mediaPlayer?.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startPlaybackProgressTracker()
                return
            }

            // New playback
            stopPlayback()

            val player = MediaPlayer()
            player.setDataSource(file.absolutePath)
            player.prepare()
            player.start()

            mediaPlayer = player
            val duration = player.duration

            _playbackState.value = PlaybackState(
                isPlaying = true,
                currentPositionMs = 0,
                durationMs = duration,
                playingPath = filePath
            )

            player.setOnCompletionListener {
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPositionMs = 0
                )
                playbackJob?.cancel()
            }

            startPlaybackProgressTracker()
        } catch (e: Exception) {
            Log.e("AudioJournalManager", "Failed to play audio", e)
            stopPlayback()
        }
    }

    fun pausePlayback() {
        try {
            mediaPlayer?.pause()
            _playbackState.value = _playbackState.value.copy(isPlaying = false)
            playbackJob?.cancel()
        } catch (e: Exception) {
            Log.e("AudioJournalManager", "Failed to pause audio", e)
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _playbackState.value = PlaybackState()
    }

    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            Log.e("AudioJournalManager", "Failed to seek audio", e)
        }
    }

    private fun startPlaybackProgressTracker() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                delay(200)
                val current = mediaPlayer?.currentPosition ?: 0
                _playbackState.value = _playbackState.value.copy(currentPositionMs = current)
            }
        }
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaRecorder = null
    }

    fun releaseAll() {
        cancelRecording()
        stopPlayback()
    }
}
