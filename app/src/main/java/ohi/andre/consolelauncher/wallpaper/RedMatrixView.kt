package ohi.andre.consolelauncher.wallpaper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

/** Red terminal matrix rain with layered depth and gentle device-tilt parallax. */
class RedMatrixView(context: Context) : View(context), SensorEventListener {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = android.graphics.Typeface.MONOSPACE }
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val columns = ArrayList<Drop>()
    private var tiltX = 0f
    private var tiltY = 0f
    private var phase = 0f
    private data class Drop(var y: Float, val speed: Float, val length: Int, val depth: Float, val seed: Int)

    init { isFocusable = false }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }
    override fun onDetachedFromWindow() { sensorManager.unregisterListener(this); super.onDetachedFromWindow() }
    override fun onSensorChanged(event: SensorEvent) {
        tiltX += ((event.values.getOrNull(1) ?: 0f) * 10f - tiltX) * .08f
        tiltY += ((event.values.getOrNull(0) ?: 0f) * 8f - tiltY) * .08f
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(2, 0, 2))
        val size = 20f
        while (columns.size < (width / size).toInt() + 2) {
            val i = columns.size
            columns.add(Drop(Random.nextFloat() * height, 2f + Random.nextFloat() * 7f, 5 + Random.nextInt(13), .35f + Random.nextFloat() * .65f, i * 97))
        }
        phase += .035f
        paint.textSize = size
        columns.forEachIndexed { i, drop ->
            drop.y += drop.speed
            if (drop.y - drop.length * size > height + 80) drop.y = -Random.nextInt(height.coerceAtLeast(1)).toFloat()
            val x = i * size + tiltX * drop.depth
            for (n in 0 until drop.length) {
                val y = drop.y - n * size
                if (y < -size || y > height + size) continue
                val alpha = ((255f * (1f - n.toFloat() / drop.length) * drop.depth).toInt()).coerceIn(20, 255)
                paint.color = Color.argb(alpha, 190 + (drop.seed % 45), 0, 0)
                val code = "01アイ#%<>[]{}"[(drop.seed + n + (phase * 10).toInt()).mod(12)]
                canvas.drawText(code.toString(), x + sin(phase + i) * tiltY * drop.depth, y, paint)
            }
        }
        postInvalidateOnAnimation()
    }
}
