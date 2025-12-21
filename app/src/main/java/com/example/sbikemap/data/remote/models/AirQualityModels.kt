package com.example.sbikemap.data.remote.models

data class AirPollutionResponse(
    val list: List<AirPollutionItem>
)

data class AirPollutionItem(
    val main: AirQualityMain,
    val components: AirQualityComponents
)

data class AirQualityMain(
    val aqi: Int // 1 = Tốt, 5 = Rất kém
)

data class AirQualityComponents(
    val pm2_5: Double, // Bụi mịn PM2.5
    val pm10: Double,  // Bụi PM10
    val co: Double,    // CO
    val no2: Double,   // NO2
    val o3: Double     // Ozone
)