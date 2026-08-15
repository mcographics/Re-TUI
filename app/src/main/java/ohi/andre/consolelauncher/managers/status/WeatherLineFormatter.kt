package ohi.andre.consolelauncher.managers.status

import java.util.Locale
import kotlin.math.roundToInt

internal data class WeatherDisplayData(
    val temperature: String,
    val temperatureUnit: String,
    val dailyHigh: String?,
    val dailyLow: String?,
    val conditionLabel: String,
    val condition: WeatherCondition,
    val symbolCode: String
)

internal enum class WeatherLineState {
    READY,
    CACHED,
    LOADING,
    UNAVAILABLE
}

internal object WeatherLineFormatter {
    const val ICON_PLACEHOLDER: Char = '\uFFFC'
    const val LONG_DIVIDER = "────────────────────────────────────────"
    const val SHORT_DIVIDER = "──────────────────────────"

    fun candidates(locationLabel: String, data: WeatherDisplayData): List<String> {
        val location = normalizedLocation(locationLabel)
        val shortLocation = location.substringBefore(' ').ifBlank { location }
        val condition = data.conditionLabel.uppercase(Locale.US)
        val temperature = withUnit(data.temperature, data.temperatureUnit)
        val compactTemperature = compactTemperature(data.temperature, data.temperatureUnit)
        val range = range(data.dailyHigh, data.dailyLow)
        val compactRange = compactRange(data.dailyHigh, data.dailyLow)

        return linkedSetOf(
            buildString {
                append(ICON_PLACEHOLDER).append("  ")
                append(location).append(" // ")
                append(condition).append(" // ")
                append(temperature)
                if (range != null) append(" // ").append(range)
            },
            buildString {
                append(ICON_PLACEHOLDER).append("  ")
                append(shortLocation).append(" // ")
                append(condition).append(" // ")
                append(compactTemperature)
                if (compactRange != null) append(" // ").append(compactRange)
            },
            "$ICON_PLACEHOLDER  $shortLocation // $compactTemperature"
        ).toList()
    }

    fun select(
        locationLabel: String,
        data: WeatherDisplayData,
        availableWidthPx: Float,
        measure: (String) -> Float
    ): String {
        val options = candidates(locationLabel, data)
        return options.firstOrNull { measure(measurementText(it)) <= availableWidthPx }
            ?: options.last()
    }

    fun stateLine(state: WeatherLineState): String = when (state) {
        WeatherLineState.LOADING -> "$ICON_PLACEHOLDER  WX // SYNC_"
        WeatherLineState.UNAVAILABLE -> "$ICON_PLACEHOLDER  WEATHER // DATA UNAVAILABLE"
        else -> "$ICON_PLACEHOLDER  WX // DATA UNAVAILABLE"
    }

    fun statusBlock(line: String): String =
        "$line\n$LONG_DIVIDER\nNOTIFICATIONS\n$SHORT_DIVIDER"

    private fun normalizedLocation(locationLabel: String): String =
        locationLabel.trim()
            .ifBlank { "WX" }
            .uppercase(Locale.US)
            .replace(Regex("\\s+"), " ")

    private fun withUnit(value: String, unit: String): String =
        if (value.isBlank()) "--°" else "$value°$unit"

    private fun compactTemperature(value: String, unit: String): String {
        val numeric = value.toDoubleOrNull()
        return if (numeric == null) "--°" else "${numeric.roundToInt()}°$unit"
    }

    private fun range(high: String?, low: String?): String? {
        if (high.isNullOrBlank() || low.isNullOrBlank()) return null
        return "H:$high° L:$low°"
    }

    private fun compactRange(high: String?, low: String?): String? {
        val highValue = high?.toDoubleOrNull()?.roundToInt() ?: return null
        val lowValue = low?.toDoubleOrNull()?.roundToInt() ?: return null
        return "H$highValue L$lowValue"
    }

    private fun measurementText(value: String): String =
        value.replace(ICON_PLACEHOLDER.toString(), "MM")

}
