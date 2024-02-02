package com.plcoding.audiorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.assemblyai.api.RealtimeTranscriber
import com.plcoding.audiorecorder.record.AudioStream

class MainActivity : ComponentActivity() {

    private var audioStream: AudioStream? = null

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
        val apiKey = BuildConfig.ASSEMBLYAI_API_KEY
        val realtimeTranscriber: RealtimeTranscriber = RealtimeTranscriber.builder()
            .apiKey(apiKey)
            .sampleRate(16000)
            .onPartialTranscript { partial ->  println(partial.text) }
            .onFinalTranscript { finalTranscript ->  println(finalTranscript.text) }
            .onError { error -> error.printStackTrace() }
            .build()

        audioStream = AudioStream(this, realtimeTranscriber)

        val listenButton: Button = findViewById(R.id.listenButton)
        listenButton.setOnClickListener {
            if (listenButton.text == "Start Listening") {
                listenButton.text = "Stop Listening"
                listenButton.setBackgroundColor(ContextCompat.getColor(this, R.color.buttercream))
                audioStream?.startRecording()
            } else {
                listenButton.text = "Start Listening"
                listenButton.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                audioStream?.stopRecording()
            }
        }
    }
}