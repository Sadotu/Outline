package com.plcoding.audiorecorder.record

class TranscriptProcessor {
    private var currentSentence = ""

    fun updateSentence(partialText: String) {
        val words = partialText.split(" ")
        if (words.isNotEmpty()) {
            val lastWord = words.last()
            if (!currentSentence.endsWith(lastWord)) {
                currentSentence = if (currentSentence.isEmpty()) {
                    lastWord
                } else {
                    "$currentSentence $lastWord"
                }
            }
        }
        println(currentSentence)
    }
}