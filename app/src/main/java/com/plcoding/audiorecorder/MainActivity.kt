package com.plcoding.audiorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.plcoding.audiorecorder.playback.AndroidAudioPlayer
import com.plcoding.audiorecorder.record.AndroidAudioRecorder
import com.plcoding.audiorecorder.ui.theme.AudioRecorderTheme
import java.io.File

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
}