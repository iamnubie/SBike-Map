package com.example.sbikemap.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

// Class quản lý việc đọc giọng nói
class VoiceHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Cài đặt ngôn ngữ Tiếng Việt
                val result = tts?.setLanguage(Locale("vi", "VN"))

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("VoiceHelper", "Ngôn ngữ tiếng Việt không được hỗ trợ trên máy này.")
                    // Fallback sang tiếng Anh nếu cần
                    tts?.language = Locale.US
                } else {
                    isReady = true
                }
            } else {
                Log.e("VoiceHelper", "Khởi tạo TTS thất bại.")
            }
        }
    }

    fun speak(text: String) {
        if (isReady && text.isNotEmpty()) {
            // QUEUE_FLUSH: Ngắt câu đang đọc dở để đọc câu mới quan trọng hơn (Ví dụ: "Rẽ phải ngay")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NavID")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}