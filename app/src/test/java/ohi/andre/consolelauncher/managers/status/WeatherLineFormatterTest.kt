package ohi.andre.consolelauncher.managers.status

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherLineFormatterTest {
    private val data = WeatherDisplayData(
        temperature = "20.4",
        temperatureUnit = "C",
        dailyHigh = "24",
        dailyLow = "16",
        conditionLabel = "Cloudy",
        condition = WeatherCondition.CLOUDY,
        symbolCode = "cloudy"
    )

    @Test
    fun buildsFullCompactAndMinimumCandidatesInPriorityOrder() {
        val candidates = WeatherLineFormatter.candidates("Gatineau QC", data)

        assertEquals("${WeatherLineFormatter.ICON_PLACEHOLDER}  GATINEAU QC // CLOUDY // 20.4°C // H:24° L:16°", candidates[0])
        assertEquals("${WeatherLineFormatter.ICON_PLACEHOLDER}  GATINEAU // CLOUDY // 20°C // H24 L16", candidates[1])
        assertEquals("${WeatherLineFormatter.ICON_PLACEHOLDER}  GATINEAU // 20°C", candidates[2])
    }

    @Test
    fun selectsTheFirstVariantThatFitsWithoutWrapping() {
        val selected = WeatherLineFormatter.select("Gatineau QC", data, 430f) { it.length * 10f }
        assertEquals(WeatherLineFormatter.candidates("Gatineau QC", data)[1], selected)

        val minimum = WeatherLineFormatter.select("Gatineau QC", data, 100f) { it.length * 10f }
        assertEquals(WeatherLineFormatter.candidates("Gatineau QC", data).last(), minimum)
    }

    @Test
    fun preservesConfiguredTemperatureUnitsAndTerminalFailureStates() {
        val fahrenheit = data.copy(temperature = "68.7", temperatureUnit = "F")
        assertTrue(WeatherLineFormatter.candidates("Gatineau QC", fahrenheit).first().contains("68.7°F"))
        assertTrue(WeatherLineFormatter.stateLine(WeatherLineState.LOADING).contains("WX // SYNC_"))
        assertTrue(WeatherLineFormatter.stateLine(WeatherLineState.UNAVAILABLE).contains("DATA UNAVAILABLE"))
    }

    @Test
    fun statusBlockUsesTheSystemMonitorRuleAsItsTopBoundary() {
        val line = "${WeatherLineFormatter.ICON_PLACEHOLDER}  GATINEAU // CLEAR // 20°C // H22 L15"
        val block = WeatherLineFormatter.statusBlock(line)

        assertTrue(block.startsWith("$line\n"))
        assertEquals(
            1,
            Regex(Regex.escape(WeatherLineFormatter.LONG_DIVIDER)).findAll(block).count()
        )
        assertTrue(
            block.endsWith(
                "${WeatherLineFormatter.LONG_DIVIDER}\nNOTIFICATIONS\n${WeatherLineFormatter.SHORT_DIVIDER}"
            )
        )
    }
}
