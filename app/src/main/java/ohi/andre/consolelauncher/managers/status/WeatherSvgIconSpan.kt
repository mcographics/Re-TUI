package ohi.andre.consolelauncher.managers.status

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.text.style.ImageSpan
import android.util.Log
import com.caverock.androidsvg.SVG
import java.util.Locale

internal object WeatherSvgIconSpan {
    fun create(
        context: Context,
        condition: WeatherCondition,
        color: Int,
        sizeDp: Int = 18
    ): ImageSpan? {
        return try {
            val pixelSize = (context.resources.displayMetrics.density * sizeDp)
                .toInt()
                .coerceAtLeast(1)
            val colorHex = String.format(Locale.US, "#%06X", color and 0x00FFFFFF)
            val source = WeatherSvgIcons.source(condition).replace("currentColor", colorHex)
            val svg = SVG.getFromString(source)
            svg.documentWidth = pixelSize.toFloat()
            svg.documentHeight = pixelSize.toFloat()

            val bitmap = Bitmap.createBitmap(pixelSize, pixelSize, Bitmap.Config.ARGB_8888)
            svg.renderToCanvas(Canvas(bitmap))
            val drawable = BitmapDrawable(context.resources, bitmap).apply {
                setBounds(0, 0, pixelSize, pixelSize)
            }
            ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
        } catch (error: Exception) {
            Log.w("TUI-WEATHER", "Unable to render inline weather SVG", error)
            null
        }
    }
}

internal object WeatherSvgIcons {
    fun source(condition: WeatherCondition): String = when (condition) {
        WeatherCondition.CLEAR_DAY -> svg(
            """<circle cx="12" cy="12" r="3.5"/>
<line x1="12" y1="2" x2="12" y2="5"/><line x1="12" y1="19" x2="12" y2="22"/>
<line x1="2" y1="12" x2="5" y2="12"/><line x1="19" y1="12" x2="22" y2="12"/>
<line x1="4.9" y1="4.9" x2="7" y2="7"/><line x1="17" y1="17" x2="19.1" y2="19.1"/>
<line x1="4.9" y1="19.1" x2="7" y2="17"/><line x1="17" y1="7" x2="19.1" y2="4.9"/>"""
        )

        WeatherCondition.CLEAR_NIGHT -> svg(
            """<path d="M19.5 15.2A8 8 0 0 1 8.8 4.5 8.5 8.5 0 1 0 19.5 15.2Z"/>"""
        )

        WeatherCondition.PARTLY_CLOUDY -> svg(
            """<circle cx="8" cy="8" r="3"/>
<line x1="8" y1="2" x2="8" y2="3.5"/><line x1="2" y1="8" x2="3.5" y2="8"/>
<line x1="3.8" y1="3.8" x2="4.9" y2="4.9"/><line x1="12.1" y1="3.8" x2="11" y2="4.9"/>
<path d="M6 18h11.2a3.3 3.3 0 0 0 .3-6.6A5.2 5.2 0 0 0 7.4 12.8 2.7 2.7 0 0 0 6 18Z"/>"""
        )

        WeatherCondition.CLOUDY -> svg(cloudPath())

        WeatherCondition.OVERCAST -> svg(
            """<path d="M4.5 13.5a3 3 0 0 1 2.8-3 5 5 0 0 1 9.6-.8 3.5 3.5 0 0 1 1.6 6.6"/>
${cloudPath()}"""
        )

        WeatherCondition.RAIN -> svg(
            """${cloudPath()}
<line x1="8" y1="17" x2="7" y2="20"/><line x1="12" y1="17" x2="11" y2="20"/><line x1="16" y1="17" x2="15" y2="20"/>"""
        )

        WeatherCondition.HEAVY_RAIN -> svg(
            """${cloudPath()}
<line x1="6" y1="17" x2="4.8" y2="21"/><line x1="10" y1="17" x2="8.8" y2="21"/>
<line x1="14" y1="17" x2="12.8" y2="21"/><line x1="18" y1="17" x2="16.8" y2="21"/>"""
        )

        WeatherCondition.THUNDERSTORM -> svg(
            """${cloudPath()}
<polyline points="13,16 10,20 13,20 11,23 17,18 14,18 16,16"/>"""
        )

        WeatherCondition.SNOW -> svg(
            """${cloudPath()}
<line x1="8" y1="17" x2="8" y2="21"/><line x1="6.3" y1="18" x2="9.7" y2="20"/><line x1="9.7" y1="18" x2="6.3" y2="20"/>
<line x1="16" y1="17" x2="16" y2="21"/><line x1="14.3" y1="18" x2="17.7" y2="20"/><line x1="17.7" y1="18" x2="14.3" y2="20"/>"""
        )

        WeatherCondition.FOG -> svg(
            """<line x1="4" y1="8" x2="20" y2="8"/><line x1="2" y1="12" x2="18" y2="12"/><line x1="6" y1="16" x2="22" y2="16"/>"""
        )

        WeatherCondition.UNKNOWN -> svg(
            """<circle cx="12" cy="12" r="9"/><path d="M9.8 9a2.5 2.5 0 1 1 3.1 2.4c-.9.4-.9 1-.9 2"/><line x1="12" y1="17" x2="12" y2="17.1"/>"""
        )
    }

    private fun cloudPath(): String =
        """<path d="M5.5 16.5h12a3.5 3.5 0 0 0 .3-7A5.5 5.5 0 0 0 7.1 11 2.8 2.8 0 0 0 5.5 16.5Z"/>"""

    private fun svg(body: String): String =
        """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">$body</svg>"""
}
