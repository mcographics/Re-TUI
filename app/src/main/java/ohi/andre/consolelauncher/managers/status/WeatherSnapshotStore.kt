package ohi.andre.consolelauncher.managers.status

import android.content.Context
import android.content.Intent

internal data class CachedWeatherSnapshot(
    val data: WeatherDisplayData,
    val savedAtMillis: Long
)

internal object WeatherSnapshotStore {
    private const val PREFS_NAME = "retui_weather_snapshot"
    private const val KEY_TEMPERATURE = "temperature"
    private const val KEY_TEMPERATURE_UNIT = "temperature_unit"
    private const val KEY_HIGH = "daily_high"
    private const val KEY_LOW = "daily_low"
    private const val KEY_CONDITION_LABEL = "condition_label"
    private const val KEY_CONDITION = "condition"
    private const val KEY_SYMBOL = "symbol"
    private const val KEY_SAVED_AT = "saved_at"

    fun save(context: Context, data: WeatherDisplayData) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_TEMPERATURE, data.temperature)
            .putString(KEY_TEMPERATURE_UNIT, data.temperatureUnit)
            .putString(KEY_HIGH, data.dailyHigh)
            .putString(KEY_LOW, data.dailyLow)
            .putString(KEY_CONDITION_LABEL, data.conditionLabel)
            .putString(KEY_CONDITION, data.condition.name)
            .putString(KEY_SYMBOL, data.symbolCode)
            .putLong(KEY_SAVED_AT, System.currentTimeMillis())
            .apply()
    }

    fun load(context: Context): CachedWeatherSnapshot? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val temperature = preferences.getString(KEY_TEMPERATURE, null) ?: return null
        val condition = runCatching {
            WeatherCondition.valueOf(preferences.getString(KEY_CONDITION, null).orEmpty())
        }.getOrDefault(WeatherCondition.UNKNOWN)
        return CachedWeatherSnapshot(
            data = WeatherDisplayData(
                temperature = temperature,
                temperatureUnit = preferences.getString(KEY_TEMPERATURE_UNIT, "C") ?: "C",
                dailyHigh = preferences.getString(KEY_HIGH, null),
                dailyLow = preferences.getString(KEY_LOW, null),
                conditionLabel = preferences.getString(KEY_CONDITION_LABEL, condition.displayName)
                    ?: condition.displayName,
                condition = condition,
                symbolCode = preferences.getString(KEY_SYMBOL, "") ?: ""
            ),
            savedAtMillis = preferences.getLong(KEY_SAVED_AT, 0L)
        )
    }
}

internal object WeatherIntentContract {
    const val EXTRA_STATE = "retui.weather.state"
    const val EXTRA_TEMPERATURE = "retui.weather.temperature"
    const val EXTRA_TEMPERATURE_UNIT = "retui.weather.temperature_unit"
    const val EXTRA_HIGH = "retui.weather.high"
    const val EXTRA_LOW = "retui.weather.low"
    const val EXTRA_CONDITION_LABEL = "retui.weather.condition_label"
    const val EXTRA_CONDITION = "retui.weather.condition"
    const val EXTRA_SYMBOL = "retui.weather.symbol"
    const val EXTRA_SAVED_AT = "retui.weather.saved_at"

    fun putSnapshot(
        intent: Intent,
        data: WeatherDisplayData,
        cached: Boolean,
        savedAtMillis: Long = System.currentTimeMillis()
    ) {
        intent.putExtra(EXTRA_STATE, if (cached) WeatherLineState.CACHED.name else WeatherLineState.READY.name)
        intent.putExtra(EXTRA_TEMPERATURE, data.temperature)
        intent.putExtra(EXTRA_TEMPERATURE_UNIT, data.temperatureUnit)
        intent.putExtra(EXTRA_HIGH, data.dailyHigh)
        intent.putExtra(EXTRA_LOW, data.dailyLow)
        intent.putExtra(EXTRA_CONDITION_LABEL, data.conditionLabel)
        intent.putExtra(EXTRA_CONDITION, data.condition.name)
        intent.putExtra(EXTRA_SYMBOL, data.symbolCode)
        intent.putExtra(EXTRA_SAVED_AT, savedAtMillis)
    }

    fun putState(intent: Intent, state: WeatherLineState) {
        intent.putExtra(EXTRA_STATE, state.name)
    }

    fun state(intent: Intent): WeatherLineState? = runCatching {
        WeatherLineState.valueOf(intent.getStringExtra(EXTRA_STATE).orEmpty())
    }.getOrNull()

    fun data(intent: Intent): WeatherDisplayData? {
        val temperature = intent.getStringExtra(EXTRA_TEMPERATURE) ?: return null
        val condition = runCatching {
            WeatherCondition.valueOf(intent.getStringExtra(EXTRA_CONDITION).orEmpty())
        }.getOrDefault(WeatherCondition.UNKNOWN)
        return WeatherDisplayData(
            temperature = temperature,
            temperatureUnit = intent.getStringExtra(EXTRA_TEMPERATURE_UNIT) ?: "C",
            dailyHigh = intent.getStringExtra(EXTRA_HIGH),
            dailyLow = intent.getStringExtra(EXTRA_LOW),
            conditionLabel = intent.getStringExtra(EXTRA_CONDITION_LABEL) ?: condition.displayName,
            condition = condition,
            symbolCode = intent.getStringExtra(EXTRA_SYMBOL).orEmpty()
        )
    }
}
