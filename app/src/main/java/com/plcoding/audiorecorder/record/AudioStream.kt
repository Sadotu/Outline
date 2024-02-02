package com.plcoding.audiorecorder.record

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import com.assemblyai.api.RealtimeTranscriber
import com.assemblyai.api.core.ApiError

class AudioStream(
    private val context: Context,
    private val realtimeTranscriber: RealtimeTranscriber
) {
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    private var microphone: AudioRecord? = null
    private var isRecording = false

    fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        realtimeTranscriber.connect()

        microphone = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            minBufferSize
        )
        microphone?.startRecording()
        isRecording = true

        Thread {
            val buffer = ByteArray(512)
            while (isRecording) {
                val readSize = microphone?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0) {
                    try {
                        val actualData = buffer.copyOfRange(0, readSize)
                        realtimeTranscriber.sendAudio(actualData)
                    } catch (error: ApiError) {
                        println(error.body())
                        println(error.statusCode())
                    }
                }
            }
            microphone?.stop()
            microphone?.release()
            microphone = null
        }.start()
    }

    fun stopRecording() {
        isRecording = false
    }
}