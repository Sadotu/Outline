package com.plcoding.audiorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.assemblyai.api.AssemblyAI
import com.plcoding.audiorecorder.playback.AndroidAudioPlayer
import com.plcoding.audiorecorder.record.AndroidAudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Properties

class MainActivity : ComponentActivity() {

    private val recorder by lazy {
        AndroidAudioRecorder(applicationContext)
    }

    private val player by lazy {
        AndroidAudioPlayer(applicationContext)
    }

    private var audioFile: File? = null

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            0
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.INTERNET),
            0
        )

        setContentView(R.layout.activity_main)
        val listenButton: Button = findViewById(R.id.listenButton)
        listenButton.setOnClickListener {
            if (listenButton.text == "Start Listening") {
                listenButton.text = "Stop Listening"
                listenButton.setBackgroundColor(ContextCompat.getColor(this, R.color.buttercream))
                File(cacheDir, "audio.mp3").also {
                    recorder.start(it)
                    audioFile = it
                }
            } else {
                listenButton.text = "Start Listening"
                listenButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                recorder.stop()
                GlobalScope.launch {
                    audioFile?.let { it1 -> transcribeAudioAsync(it1) }
                }
            }
        }

        val playButton: Button = findViewById(R.id.playButton)
        playButton.setOnClickListener{
            if (playButton.text == "Play") {
                playButton.text = "Stop"
                player.playFile(audioFile ?: return@setOnClickListener)
            } else {
                playButton.text = "Play"
                player.stop()
            }
        }
    }

    private suspend fun transcribeAudioAsync(audioFile: File) {
        coroutineScope {
            val apiKey = BuildConfig.ASSEMBLYAI_API_KEY

            val aai = AssemblyAI.builder()
                .apiKey(apiKey)
                .build()

            // Launch a new coroutine in the background and continue
            val transcriptDeferred = async(Dispatchers.IO) {
                aai.transcripts().transcribe(audioFile)
            }

            // Wait for the result of the background coroutine
            val transcript = transcriptDeferred.await()

            println("Received response!${transcript.text}")
        }
    }
}