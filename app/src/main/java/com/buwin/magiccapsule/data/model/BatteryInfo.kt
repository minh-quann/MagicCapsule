package com.buwin.magiccapsule.data.model

/**
 * Snapshot of real-time battery electrical metrics and charging state.
 */
data class BatteryInfo(
    val powerW: Double = 0.0,
    val currentMa: Long = 0L,
    val voltageV: Double = 0.0,
    val levelPercent: Int = 0,
    val temperatureC: Double = 0.0,
    val isCharging: Boolean = false,
    val isFull: Boolean = false,
    val pluggedType: PluggedType = PluggedType.NONE
)

enum class PluggedType {
    NONE,
    AC,
    USB,
    WIRELESS
}
