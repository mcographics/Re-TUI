package ohi.andre.consolelauncher.managers.status

import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherSvgIconsTest {
    @Test
    fun everyNormalizedConditionHasAMonochromeOutlineSvg() {
        WeatherCondition.entries.forEach { condition ->
            val svg = WeatherSvgIcons.source(condition)
            assertTrue("Missing SVG root for $condition", svg.startsWith("<svg"))
            assertTrue("Icon must inherit terminal color for $condition", svg.contains("stroke=\"currentColor\""))
            assertTrue("Icon must remain outline-only for $condition", svg.contains("fill=\"none\""))
            assertTrue("Icon must use the required thin stroke for $condition", svg.contains("stroke-width=\"1.5\""))
        }
    }
}
