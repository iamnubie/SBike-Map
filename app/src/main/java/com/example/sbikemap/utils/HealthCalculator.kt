package com.example.sbikemap.utils

import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

object HealthCalculator {

    /**
     * 1. TÍNH LƯỢNG CALO TIÊU THỤ (Dựa trên chỉ số METs)
     * @param weightKg: Cân nặng người (kg)
     * @param durationMinutes: Thời gian đạp (phút)
     * @param avgSpeedKmh: Tốc độ trung bình (km/h)
     */
    fun calculateCalories(weightKg: Double, durationMinutes: Double, avgSpeedKmh: Double): Double {
        // Chỉ số METs theo tốc độ đạp xe (Tham khảo Compendium of Physical Activities)
        val mets = when {
            avgSpeedKmh < 16.0 -> 4.0   // Đạp chậm (<10mph) - Nhẹ
            avgSpeedKmh < 20.0 -> 6.0   // Trung bình nhẹ
            avgSpeedKmh < 24.0 -> 8.0   // Trung bình
            avgSpeedKmh < 30.0 -> 10.0  // Nhanh
            else -> 12.0                // Rất nhanh (Đua xe)
        }

        // Công thức chuẩn: Calories = METs * Cân nặng (kg) * Thời gian (giờ)
        val durationHours = durationMinutes / 60.0
        return mets * weightKg * durationHours
    }

    /**
     * 2. TÍNH CÔNG SUẤT ƯỚC TÍNH (WATTS) - Quan trọng với dân chuyên nghiệp
     * @param velocityMs: Tốc độ (m/s)
     * @param grade: Độ dốc (dạng thập phân, VD: 5% = 0.05). Lấy từ (Elevation Gain / Distance)
     * @param totalWeightKg: Cân nặng người + xe (kg)
     */
    fun calculateEstimatedPower(velocityMs: Double, grade: Double, totalWeightKg: Double): Double {
        if (velocityMs <= 0) return 0.0

        // Các hằng số vật lý (Mặc định cho xe Road trên đường nhựa)
        val gravity = 9.81          // Gia tốc trọng trường (m/s^2)
        val rollingResistance = 0.005 // Hệ số ma sát lốp (Crr)
        val airDensity = 1.225      // Mật độ không khí (kg/m^3)
        val dragCoefficient = 0.9   // Hệ số cản gió (CdA) - Diện tích cản gió hiệu dụng

        val angle = atan(grade) // Góc dốc (radians)

        // 1. Công suất thắng trọng lực (khi leo dốc)
        // P_gravity = m * g * sin(angle) * v
        val pGravity = totalWeightKg * gravity * sin(angle) * velocityMs

        // 2. Công suất thắng ma sát lăn
        // P_rolling = m * g * cos(angle) * Crr * v
        val pRolling = totalWeightKg * gravity * cos(angle) * rollingResistance * velocityMs

        // 3. Công suất thắng lực cản gió (Tăng lũy thừa 3 theo tốc độ)
        // P_drag = 0.5 * density * CdA * v^3
        val pDrag = 0.5 * airDensity * dragCoefficient * velocityMs.pow(3)

        // Tổng công suất (Watts)
        val totalPower = pGravity + pRolling + pDrag

        // Công suất không thể âm (trừ khi đang xuống dốc thả trôi thì coi như 0)
        return if (totalPower > 0) totalPower else 0.0
    }

    /**
     * 3. TÍNH VAM (Tốc độ leo cao - Vertical Ascent Meters/Hour)
     * Chỉ số "Vua" của dân leo núi (Climbers).
     * @param elevationGainMeters: Tổng độ cao đã leo (m)
     * @param durationMinutes: Thời gian đạp (phút)
     */
    fun calculateVAM(elevationGainMeters: Double, durationMinutes: Double): Double {
        if (durationMinutes <= 0) return 0.0
        // Công thức: (Độ cao leo được / Số phút) * 60 phút
        return (elevationGainMeters * 60.0) / durationMinutes
    }

    /**
     * 4. TÍNH NHU CẦU NƯỚC (Hydration Needs)
     * @param durationMinutes: Thời gian đạp (phút)
     * @param isHotWeather: Thời tiết có nóng không? (Trên 28 độ C)
     * @return Chuỗi gợi ý (VD: "500 ml")
     */
    fun calculateWaterNeeds(durationMinutes: Double, isHotWeather: Boolean): String {
        // Trung bình cơ thể mất 500ml - 1000ml nước mỗi giờ khi vận động
        val lossPerHourMl = if (isHotWeather) 1000.0 else 500.0

        val totalLossMl = (lossPerHourMl / 60.0) * durationMinutes

        // Làm tròn đến hàng chục
        return "${totalLossMl.toInt()} ml"
    }
}