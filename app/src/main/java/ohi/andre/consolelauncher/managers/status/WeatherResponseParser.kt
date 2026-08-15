package ohi.andre.consolelauncher.managers.status

import com.jayway.jsonpath.JsonPath
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal enum class WeatherCondition(val key: String, val displayName: String) {
    CLEAR_DAY("clear", "Clear"),
    CLEAR_NIGHT("clearNight", "Clear"),
    PARTLY_CLOUDY("partlyCloudy", "Partly cloudy"),
    CLOUDY("cloudy", "Cloudy"),
    OVERCAST("overcast", "Overcast"),
    RAIN("rain", "Rain"),
    HEAVY_RAIN("heavyRain", "Heavy rain"),
    THUNDERSTORM("thunderstorm", "Thunderstorm"),
    SNOW("snow", "Snow"),
    FOG("fog", "Fog"),
    UNKNOWN("unknown", "Unknown")
}

internal data class WeatherSnapshot(
    val values: Map<String, String>,
    val symbolCode: String,
    val condition: WeatherCondition
) {
    fun displayData(): WeatherDisplayData = WeatherDisplayData(
        temperature = values["temp"].orEmpty(),
        temperatureUnit = values["unit"] ?: "C",
        dailyHigh = values["high"],
        dailyLow = values["low"],
        conditionLabel = condition.displayName,
        condition = condition,
        symbolCode = symbolCode
    )
}

internal object WeatherResponseParser {
    fun parse(json: String, temperatureMeasure: String): WeatherSnapshot? {
        return try {
            val document = JsonPath.parse(json)
            val details = "$.properties.timeseries[0].data.instant.details"
            val symbol = readString(document, "$.properties.timeseries[0].data.next_1_hours.summary.symbol_code")
                ?: readString(document, "$.properties.timeseries[0].data.next_6_hours.summary.symbol_code")
                ?: return null
            val temperature = readNumber(document, "$details.air_temperature") ?: return null
            val condition = mapCondition(symbol)
            val values = linkedMapOf(
                "main" to condition.displayName,
                "description" to condition.displayName,
                "condition" to condition.displayName,
                "temp" to number(convertTemperature(temperature, temperatureMeasure)),
                "unit" to temperatureUnit(temperatureMeasure),
                "symbol_code" to symbol
            )
            forecastRange(document, temperatureMeasure)?.let { (low, high) ->
                values["high"] = number(high)
                values["low"] = number(low)
            }
            addNumber(values, "pressure", document, "$details.air_pressure_at_sea_level")
            addNumber(values, "humidity", document, "$details.relative_humidity")
            addNumber(values, "deg", document, "$details.wind_from_direction")
            addNumber(values, "clouds", document, "$details.cloud_area_fraction")
            addNumber(values, "all", document, "$details.cloud_area_fraction")
            readNumber(document, "$details.wind_speed")?.let {
                values["speed"] = number(if (temperatureMeasure == "imperial") it * 2.236936 else it)
            }
            WeatherSnapshot(values, symbol, condition)
        } catch (_: Exception) {
            null
        }
    }

    fun parseCoordinates(value: String?): Pair<Double, Double>? {
        val parts = value?.trim()?.split(',') ?: return null
        if (parts.size != 2) return null
        val latitude = parts[0].trim().toDoubleOrNull() ?: return null
        val longitude = parts[1].trim().toDoubleOrNull() ?: return null
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
        return latitude to longitude
    }

    fun ascii(symbolCode: String?): String {
        val symbol = symbolCode.orEmpty()
        return when {
            "thunder" in symbol -> "    .-.\n   (   ).\n  (___(__)\n   /_/ /_/"
            "snow" in symbol -> "    .-.\n   (   ).\n  (___(__)\n   *  *  *"
            "rain" in symbol || "sleet" in symbol -> "    .-.\n   (   ).\n  (___(__)\n   / / / /"
            symbol == "fog" -> " _ - _ - _\n  _ - _ -\n _ - _ - _"
            symbol.startsWith("partlycloudy") -> "   \\  /\n _ /\"\".-.\n   \\_(   ).\n   /(___(__)"
            symbol == "cloudy" -> "    .--.\n .-(    ).\n(___.__)__)"
            symbol.startsWith("fair") || symbol.startsWith("clearsky") -> "   \\ | /\n    .-.\n --(   )--\n    `-'"
            else -> ""
        }
    }

    fun mapCondition(symbolCode: String?): WeatherCondition {
        val raw = symbolCode.orEmpty().lowercase(Locale.US)
        val symbol = raw.removeSuffix("_day").removeSuffix("_night").removeSuffix("_polartwilight")
        return when {
            "thunder" in symbol -> WeatherCondition.THUNDERSTORM
            "snow" in symbol || "sleet" in symbol -> WeatherCondition.SNOW
            symbol.startsWith("heavy") && "rain" in symbol -> WeatherCondition.HEAVY_RAIN
            "rain" in symbol -> WeatherCondition.RAIN
            symbol == "fog" -> WeatherCondition.FOG
            symbol == "partlycloudy" || symbol == "fair" -> WeatherCondition.PARTLY_CLOUDY
            symbol == "cloudy" -> WeatherCondition.OVERCAST
            symbol == "clearsky" && raw.endsWith("_night") -> WeatherCondition.CLEAR_NIGHT
            symbol == "clearsky" -> WeatherCondition.CLEAR_DAY
            "cloud" in symbol -> WeatherCondition.CLOUDY
            else -> WeatherCondition.UNKNOWN
        }
    }

    private fun forecastRange(
        document: com.jayway.jsonpath.DocumentContext,
        temperatureMeasure: String
    ): Pair<Double, Double>? {
        val series = try {
            document.read<List<Map<String, Any?>>>("$.properties.timeseries")
        } catch (_: Exception) {
            return null
        }
        if (series.isEmpty()) return null

        val start = parseTime(series.first()["time"] as? String)
        val cutoff = start?.plus(FORECAST_WINDOW_MS)
        val temperatures = ArrayList<Double>()

        for (entry in series) {
            val timestamp = parseTime(entry["time"] as? String)
            if (cutoff != null && timestamp != null && timestamp > cutoff) break

            val data = entry["data"] as? Map<*, *> ?: continue
            val instant = data["instant"] as? Map<*, *>
            val instantDetails = instant?.get("details") as? Map<*, *>
            numberFrom(instantDetails, "air_temperature")?.let(temperatures::add)

            for (period in PERIOD_KEYS) {
                val block = data[period] as? Map<*, *> ?: continue
                val periodDetails = block["details"] as? Map<*, *> ?: continue
                numberFrom(periodDetails, "air_temperature_min")?.let(temperatures::add)
                numberFrom(periodDetails, "air_temperature_max")?.let(temperatures::add)
            }
        }

        if (temperatures.isEmpty()) return null
        val low = convertTemperature(temperatures.minOrNull()!!, temperatureMeasure)
        val high = convertTemperature(temperatures.maxOrNull()!!, temperatureMeasure)
        return low to high
    }

    private fun numberFrom(values: Map<*, *>?, key: String): Double? =
        (values?.get(key) as? Number)?.toDouble()

    private fun parseTime(value: String?): Long? {
        if (value.isNullOrBlank()) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(value)?.time
        } catch (_: Exception) {
            null
        }
    }

    private fun convertTemperature(celsius: Double, measure: String): Double = when (measure) {
        "imperial" -> celsius * 9.0 / 5.0 + 32.0
        "standard" -> celsius + 273.15
        else -> celsius
    }

    private fun temperatureUnit(measure: String): String = when (measure) {
        "imperial" -> "F"
        "standard" -> "K"
        else -> "C"
    }

    private fun addNumber(
        values: MutableMap<String, String>,
        name: String,
        document: com.jayway.jsonpath.DocumentContext,
        path: String
    ) {
        readNumber(document, path)?.let { values[name] = number(it) }
    }

    private fun readNumber(document: com.jayway.jsonpath.DocumentContext, path: String): Double? =
        try {
            (document.read<Any>(path) as? Number)?.toDouble()
        } catch (_: Exception) {
            null
        }

    private fun readString(document: com.jayway.jsonpath.DocumentContext, path: String): String? =
        try {
            document.read<String>(path)
        } catch (_: Exception) {
            null
        }

    private fun number(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)

    private const val FORECAST_WINDOW_MS = 24L * 60L * 60L * 1000L
    private val PERIOD_KEYS = arrayOf("next_1_hours", "next_6_hours", "next_12_hours")
}
