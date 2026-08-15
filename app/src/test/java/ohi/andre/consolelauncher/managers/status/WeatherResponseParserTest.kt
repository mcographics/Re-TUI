package ohi.andre.consolelauncher.managers.status

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherResponseParserTest {
    private val response = """{
        "properties":{"timeseries":[{"data":{
          "instant":{"details":{"air_temperature":29.7,"air_pressure_at_sea_level":1003.6,"relative_humidity":79.6,"cloud_area_fraction":96.1,"wind_from_direction":204.5,"wind_speed":4.9}},
          "next_1_hours":{"summary":{"symbol_code":"partlycloudy_night"}}
        }}]}
    }"""

    @Test
    fun parsesMetForecastIntoLegacyWeatherFields() {
        val snapshot = WeatherResponseParser.parse(response, "metric")!!

        assertEquals("Partly cloudy", snapshot.values["main"])
        assertEquals("29.7", snapshot.values["temp"])
        assertEquals("29.7", snapshot.values["high"])
        assertEquals("29.7", snapshot.values["low"])
        assertEquals("C", snapshot.values["unit"])
        assertEquals("79.6", snapshot.values["humidity"])
        assertEquals("partlycloudy_night", snapshot.symbolCode)
        assertEquals(WeatherCondition.PARTLY_CLOUDY, snapshot.condition)
        assertTrue(WeatherResponseParser.ascii(snapshot.symbolCode).contains(".-."))
    }

    @Test
    fun convertsTemperatureAndValidatesFixedCoordinates() {
        assertEquals("85.5", WeatherResponseParser.parse(response, "imperial")!!.values["temp"])
        assertEquals("F", WeatherResponseParser.parse(response, "imperial")!!.values["unit"])
        assertEquals(13.0827 to 80.2707, WeatherResponseParser.parseCoordinates("13.0827, 80.2707"))
        assertNull(WeatherResponseParser.parseCoordinates("127954"))
        assertNull(WeatherResponseParser.parseCoordinates("91,0"))
    }

    @Test
    fun normalizesProviderSymbolsWithoutDependingOnDisplayText() {
        assertEquals(WeatherCondition.CLEAR_DAY, WeatherResponseParser.mapCondition("clearsky_day"))
        assertEquals(WeatherCondition.CLEAR_NIGHT, WeatherResponseParser.mapCondition("clearsky_night"))
        assertEquals(WeatherCondition.HEAVY_RAIN, WeatherResponseParser.mapCondition("heavyrain_day"))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherResponseParser.mapCondition("rainandthunder"))
        assertEquals(WeatherCondition.SNOW, WeatherResponseParser.mapCondition("lightsnowshowers_night"))
        assertEquals(WeatherCondition.FOG, WeatherResponseParser.mapCondition("fog"))
        assertEquals(WeatherCondition.UNKNOWN, WeatherResponseParser.mapCondition("provider_future_code"))
    }

    @Test
    fun calculatesHighAndLowFromTheNextTwentyFourHoursOnly() {
        val forecast = """{
          "properties":{"timeseries":[
            {"time":"2026-08-15T12:00:00Z","data":{"instant":{"details":{"air_temperature":20}},"next_1_hours":{"summary":{"symbol_code":"cloudy"}}}},
            {"time":"2026-08-15T18:00:00Z","data":{"instant":{"details":{"air_temperature":24}}}},
            {"time":"2026-08-16T06:00:00Z","data":{"instant":{"details":{"air_temperature":10}}}},
            {"time":"2026-08-16T13:00:00Z","data":{"instant":{"details":{"air_temperature":31}}}}
          ]}}
        """.trimIndent()

        val snapshot = WeatherResponseParser.parse(forecast, "metric")!!
        assertEquals("24", snapshot.values["high"])
        assertEquals("10", snapshot.values["low"])
    }
}
