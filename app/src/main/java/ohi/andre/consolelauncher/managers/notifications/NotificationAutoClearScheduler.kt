package ohi.andre.consolelauncher.managers.notifications

internal class NotificationAutoClearScheduler(
    private val postDelayed: (Runnable, Long) -> Unit,
    private val removeCallbacks: (Runnable) -> Unit,
    private val clearNotifications: () -> Unit
) {
    private val clearRunnable = object : Runnable {
        override fun run() {
            clearNotifications()
            postDelayed(this, INTERVAL_MS)
        }
    }

    fun start() {
        removeCallbacks(clearRunnable)
        postDelayed(clearRunnable, INTERVAL_MS)
    }

    fun stop() {
        removeCallbacks(clearRunnable)
    }

    companion object {
        const val INTERVAL_MS = 5L * 60L * 1000L
    }
}
