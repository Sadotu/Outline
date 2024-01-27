package com.plcoding.audiorecorder

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.assemblyai.api.AssemblyAI
import com.assemblyai.api.RealtimeTranscriber
import com.assemblyai.api.resources.realtime.types.FinalTranscript
import com.assemblyai.api.resources.realtime.types.PartialTranscript
import com.assemblyai.api.resources.transcripts.types.Transcript
import com.plcoding.audiorecorder.playback.AndroidAudioPlayer
import com.plcoding.audiorecorder.record.AndroidAudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                lifecycleScope.launch {
                    audioFile?.let { it1 -> transcribeAudioFile(it1) }
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

    private suspend fun transcribeAudioFile(audioFile: File): String {
//        val apiKey = BuildConfig.ASSEMBLYAI_API_KEY
//
//        val aai = AssemblyAI.builder()
//            .apiKey(apiKey)
//            .build()
//
//        val transcript = aai.transcripts().transcribe(audioFile)
//
//        return transcript

        return coroutineScope {
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
            var text = "No transcript returned (yet?)"

            if (transcript.text.isPresent) {
                text = transcript.text.get()
            }

            Log.d("transcript text", text)

            return@coroutineScope text
        }
    }

//    private suspend fun transcribeAudioFile(audioFile: File): YourTranscriptType? {
//        val apiKey = BuildConfig.ASSEMBLYAI_API_KEY
//
//        val aai = AssemblyAI.builder()
//            .apiKey(apiKey)
//            .build()
//
//        var result: YourTranscriptType? = null
//
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = aai.transcripts().transcribe(audioFile)
//                if (response is Closeable) {
//                    response.use {
//                        // Process and store the result
//                        result = it // Assuming 'it' is the transcript
//                    }
//                } else {
//                    // Just process the response
//                    result = response
//                }
//            } catch (e: Exception) {
//                // Handle exceptions
//                null
//            } finally {
//                // Perform any necessary clean-up
//                // If you had opened any other resources, close them here
//            }
//            result
//        }
//    }

    private lateinit var realtimeTranscriber: RealtimeTranscriber

    private fun setupAndStartRealtimeTranscription() {
        // Initialize the RealtimeTranscriber
        val realtime = RealtimeTranscriber.builder()
            .apiKey(BuildConfig.ASSEMBLYAI_API_KEY)
            .onPartialTranscript { partial: PartialTranscript? ->
                println(
                    partial
                )
            }
            .onFinalTranscript { finalTranscript: FinalTranscript? ->
                println(
                    finalTranscript
                )
            }
            .build()

        // Start the transcription process

        launchTranscription(realtime)
    }

    private fun launchTranscription(realtime: RealtimeTranscriber) = lifecycleScope.launch(Dispatchers.IO) {
        realtime.sendAudio(byteArrayOf())
    }

    private suspend fun handleTranscript(transcript: PartialTranscript) = withContext(Dispatchers.Main) {
        // Update UI or handle the transcript on the main thread
        println("You: $transcript")
    }

    private fun stopRealtimeTranscription() {
        realtimeTranscriber.close()
    }
}