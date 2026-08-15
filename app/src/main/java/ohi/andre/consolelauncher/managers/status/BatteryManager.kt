package ohi.andre.consolelauncher.managers.status

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import ohi.andre.consolelauncher.UIManager
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager
import ohi.andre.consolelauncher.managers.xml.options.Behavior
import ohi.andre.consolelauncher.managers.xml.options.Theme
import ohi.andre.consolelauncher.managers.xml.options.Ui
import ohi.andre.consolelauncher.tuils.Tuils
import ohi.andre.consolelauncher.tuils.UIUtils.span
import ohi.andre.consolelauncher.tuils.interfaces.OnBatteryUpdate
import java.util.regex.Pattern
import java.util.regex.Matcher
import ohi.andre.consolelauncher.tuils.UIUtils

class BatteryManager(
    context: Context,
    private val size: Int,
    private val mediumPercentage: Int,
    private val lowPercentage: Int,
    private val listener: StatusUpdateListener?
) : OnBatteryUpdate {
    private val context: Context

    private var optionalCharging: Pattern? = null
    private val value: Pattern = Pattern.compile("%v", Pattern.LITERAL or Pattern.CASE_INSENSITIVE)

    private var manyStatus = false
    private var loaded = false
    private var colorHigh = 0
    private var colorMedium = 0
    private var colorLow = 0
    private var batteryFormat: String? = null

    private var charging = false
    private var lastPercentage = -1f
    private var registered = false

    private val batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percentage = level * 100 / scale.toFloat()
            update(percentage)
        }
    }

    init {
        this.context = context.getApplicationContext()
    }

    fun start() {
        if (registered) {
            return
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ContextCompat.registerReceiver(
            context,
            batteryReceiver,
            filter,
            // ACTION_BATTERY_CHANGED is sent by Android outside this app. Samsung's
            // Android 13 build rejects it when this receiver is marked not exported.
            ContextCompat.RECEIVER_EXPORTED
        )
        registered = true
    }

    fun stop() {
        if (!registered) {
            return
        }
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
        registered = false
    }

    override fun update(p: Float) {
        var p = p
        if (p == -1f) p = lastPercentage
        lastPercentage = p

        if (batteryFormat == null) {
            batteryFormat = XMLPrefsManager.get(Behavior.battery_format)

            val sep = XMLPrefsManager.get(Behavior.optional_values_separator)
            val quotedSep = Pattern.quote(sep)
            val optional = "%\\(([^" + quotedSep + "]*)" + quotedSep + "([^)]*)\\)"
            optionalCharging = Pattern.compile(optional, Pattern.CASE_INSENSITIVE)
        }

        if (!loaded) {
            loaded = true
            manyStatus = XMLPrefsManager.getBoolean(Ui.enable_battery_status)
            colorHigh = XMLPrefsManager.getColor(Theme.battery_text_high)
            colorMedium = XMLPrefsManager.getColor(Theme.battery_text_medium)
            colorLow = XMLPrefsManager.getColor(Theme.battery_text_low)
        }

        val percentage = p.toInt()

        if (XMLPrefsManager.getBoolean(Behavior.battery_progress_bar)) {
            val length = XMLPrefsManager.getInt(Behavior.battery_progress_bar_length)
            val symbol = XMLPrefsManager.get(Behavior.battery_progress_bar_symbol)
            val fullColor = colorHigh
            val emptyColor = colorLow

            val fullCount = Math.round((p / 100f) * length)
            val emptyCount = length - fullCount

            val fullPart = StringBuilder()
            for (i in 0..<fullCount) fullPart.append(symbol)

            val emptyPart = StringBuilder()
            for (i in 0..<emptyCount) emptyPart.append(symbol)

            val ssb = SpannableStringBuilder()
            ssb.append("[")
            if (fullPart.length > 0) {
                val start = ssb.length
                ssb.append(fullPart)
                ssb.setSpan(
                    ForegroundColorSpan(fullColor),
                    start,
                    ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            if (emptyPart.length > 0) {
                val start = ssb.length
                ssb.append(emptyPart)
                ssb.setSpan(
                    ForegroundColorSpan(emptyColor),
                    start,
                    ssb.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            ssb.append("] ").append(percentage.toString()).append("%")

            if (listener != null) {
                listener.onUpdate(UIManager.Label.battery, ssb)
            }
            return
        }

        val color: Int
        if (manyStatus) {
            if (percentage > mediumPercentage) color = colorHigh
            else if (percentage > lowPercentage) color = colorMedium
            else color = colorLow
        } else {
            color = colorHigh
        }

        var cp = batteryFormat
        val m = optionalCharging!!.matcher(cp)
        while (m.find()) {
            cp = cp!!.replace(
                m.group(0),
                if (m.groupCount() == 2) m.group(if (charging) 1 else 2) else Tuils.EMPTYSTRING
            )
        }

        cp = value.matcher(cp).replaceAll(percentage.toString())
        cp = Tuils.patternNewline.matcher(cp).replaceAll(Tuils.NEWLINE)

        if (listener != null) {
            listener.onUpdate(UIManager.Label.battery, span(context, cp, color, size))
        }
    }

    override fun onCharging() {
        charging = true
        update(-1f)
    }

    override fun onNotCharging() {
        charging = false
        update(-1f)
    }
}
