package com.example.sbikemap.utils

import com.google.ai.client.generativeai.GenerativeModel
import com.mapbox.navigation.base.route.NavigationRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AiRouteAnalysis(
    val bestRouteIndex: Int, // Index của tuyến đường trong list (0, 1, hay 2)
    val reasoning: String // Lý do: "Đường này ít tắc hơn..."
)

object AiRouteService {
    // Thay bằng API KEY Gemini của bạn
    private const val API_KEY = "YOUR_GEMINI_API_KEY"

    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash", // Bản Flash nhanh và rẻ
        apiKey = API_KEY
    )

    suspend fun analyzeRoutes(
        routes: List<NavigationRoute>,
        weatherDescription: String // VD: "Nắng nhẹ, 30 độ"
    ): AiRouteAnalysis = withContext(Dispatchers.IO) {

        // 1. Tạo prompt mô tả các tuyến đường
        val routesDescription = routes.mapIndexed { index, route ->
            val durationMin = route.directionsRoute.duration() / 60
            val distanceKm = route.directionsRoute.distance() / 1000
            // Mapbox có trả về typical_congestion nhưng xử lý hơi phức tạp,
            // ở đây ta giả định dựa trên duration/distance
            "Route $index: ${String.format("%.1f", distanceKm)} km, takes ${durationMin.toInt()} minutes."
        }.joinToString("\n")

        val prompt = """
            Act as an expert cycling guide. I have ${routes.size} route options.
            Current weather: $weatherDescription.
            
            $routesDescription
            
            Criteria for best cycling route:
            1. Low traffic (implied by longer duration for shorter distance).
            2. Safety and fewer turns.
            3. Weather impact (if raining, shortest is best).
            
            Task: Select the BEST route index for a biker. 
            Output strictly in this format: "INDEX|REASON" (e.g., "1|This route is slightly longer but avoids the main highway.").
            Do not output markdown.
        """.trimIndent()

        try {
            val response = generativeModel.generateContent(prompt)
            val text = response.text?.trim() ?: "0|AI unavailable"

            // Parse kết quả
            val parts = text.split("|")
            val index = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val reason = parts.getOrNull(1) ?: "Được đề xuất bởi hệ thống."

            AiRouteAnalysis(index, reason)
        } catch (e: Exception) {
            e.printStackTrace()
            AiRouteAnalysis(0, "Lỗi kết nối AI, sử dụng tuyến đường mặc định.")
        }
    }
}