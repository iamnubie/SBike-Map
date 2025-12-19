package com.example.sbikemap.utils

import com.example.sbikemap.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Data class cho tin nhắn chat
data class ChatMessage(
    val role: String,
    val content: String,
    val isUser: Boolean = role == "user"
)

object AIJourneyPlanner {

    private const val API_KEY = BuildConfig.OPENAI_API_KEY

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Tăng timeout vì tính toán health lâu
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    // 1. Hàm lập lịch ban đầu (Có thêm tham số sức khỏe)
    suspend fun planJourney(
        originName: String,
        destinationName: String,
        distanceMeters: Double,
        userWeightKg: Double, // Cân nặng user
    ): String {
        return withContext(Dispatchers.IO) {
            val distanceKm = String.format("%.1f", distanceMeters / 1000)

            val systemContent = """
                Bạn là chuyên gia huấn luyện xe đạp và dinh dưỡng thể thao.
                Nhiệm vụ: Lập lịch trình đạp xe chi tiết.
                
                QUY TẮC TRÌNH BÀY (BẮT BUỘC):
                1. Trả về văn bản thuần (Plain Text). 
                2. TUYỆT ĐỐI KHÔNG sử dụng ký tự định dạng Markdown như dấu thăng (#) hay dấu sao (*).
                3. Các tiêu đề chính hãy viết IN HOA để làm nổi bật (Ví dụ: thay vì viết "**Chặng 1**", hãy viết "CHẶNG 1").
                4. Sử dụng dấu gạch ngang (-) hoặc số (1., 2.) cho các danh sách.
                
                Yêu cầu đầu ra bắt buộc:
                1. Chia hành trình thành các chặng hợp lý.
                2. Với MỖI chặng, phải tính toán cụ thể:
                   - Khoảng cách & Thời gian dự kiến.
                   - Lượng Calo tiêu thụ (Kcal) dựa trên cân nặng $userWeightKg kg.
                   - Lượng nước cần nạp.
                3. Đưa ra lời khuyên phục hồi sức khỏe sau chuyến đi.
                
                Phong cách: Chuyên nghiệp, ngắn gọn, tập trung vào số liệu.
            """.trimIndent()

            val userContent = """
                Lập lịch trình từ: $originName đến $destinationName.
                Tổng quãng đường: $distanceKm km.
                Thông tin rider: Nặng $userWeightKg kg.
            """.trimIndent()

            return@withContext callOpenAI(listOf(
                ChatMessage("system", systemContent),
                ChatMessage("user", userContent)
            ))
        }
    }

    // 2. Hàm Chat tiếp theo (Gửi kèm lịch sử)
    suspend fun chatWithAI(history: List<ChatMessage>): String {
        return withContext(Dispatchers.IO) {
            return@withContext callOpenAI(history)
        }
    }

    // Hàm gọi API chung (Private để tái sử dụng)
    private fun callOpenAI(messages: List<ChatMessage>): String {
        try {
            val jsonBody = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    messages.forEach { msg ->
                        put(JSONObject().apply {
                            put("role", msg.role)
                            put("content", msg.content)
                        })
                    }
                })
                put("temperature", 0.7)
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $API_KEY")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                return jsonResponse.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } else {
                return "Lỗi AI: ${response.code}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "Lỗi kết nối: ${e.message}"
        }
    }
}