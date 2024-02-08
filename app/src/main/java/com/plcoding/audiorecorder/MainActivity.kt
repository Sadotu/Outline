package com.plcoding.audiorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.assemblyai.api.RealtimeTranscriber
import com.assemblyai.api.resources.transcripts.types.TranscriptLanguageCode
import com.assemblyai.api.resources.transcripts.types.TranscriptOptionalParams
import com.plcoding.audiorecorder.record.AudioStream

class MainActivity : ComponentActivity() {

    private var audioStream: AudioStream? = null
    private lateinit var yourTranscription: TextView
    private var lastPartial = "You: "

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

        var params = TranscriptOptionalParams.builder()
            .languageCode(TranscriptLanguageCode.NL)
            .build();

        setContentView(R.layout.activity_main)
        yourTranscription = findViewById(R.id.yourTranscription)
        val apiKey = BuildConfig.ASSEMBLYAI_API_KEY
        val realtimeTranscriber: RealtimeTranscriber = RealtimeTranscriber.builder()
            .apiKey(apiKey)
            .sampleRate(16000)

            .onPartialTranscript { partial ->
                val newWords = partial.text.removePrefix(lastPartial)
                lastPartial = partial.text
                runOnUiThread {
                    yourTranscription.append(newWords)
                }
            }
            .onFinalTranscript { finalTranscript ->
                runOnUiThread {
                    yourTranscription.text = "You: " + finalTranscript.text
                }
                lastPartial = "You: "
            }

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