package ohi.andre.consolelauncher.managers.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class NotificationAutoClearSchedulerTest {
    @Test
    fun `start schedules the first notification clear after five minutes`() {
        val scheduled = mutableListOf<Pair<Runnable, Long>>()
        val removed = mutableListOf<Runnable>()
        var clearCount = 0
        val scheduler = NotificationAutoClearScheduler(
            postDelayed = { runnable, delay -> scheduled += runnable to delay },
            removeCallbacks = { removed += it },
            clearNotifications = { clearCount++ }
        )

        scheduler.start()

        assertEquals(0, clearCount)
        assertEquals(1, removed.size)
        assertEquals(1, scheduled.size)
        assertEquals(5L * 60L * 1000L, scheduled.single().second)
        assertSame(removed.single(), scheduled.single().first)
    }

    @Test
    fun `scheduled clear repeats at the five minute interval`() {
        val scheduled = mutableListOf<Pair<Runnable, Long>>()
        var clearCount = 0
        val scheduler = NotificationAutoClearScheduler(
            postDelayed = { runnable, delay -> scheduled += runnable to delay },
            removeCallbacks = {},
            clearNotifications = { clearCount++ }
        )

        scheduler.start()
        val firstScheduledClear = scheduled.removeAt(0)
        firstScheduledClear.first.run()

        assertEquals(1, clearCount)
        assertEquals(1, scheduled.size)
        assertEquals(NotificationAutoClearScheduler.INTERVAL_MS, scheduled.single().second)
        assertSame(firstScheduledClear.first, scheduled.single().first)
    }

    @Test
    fun `stop removes the pending clear`() {
        val scheduled = mutableListOf<Runnable>()
        val removed = mutableListOf<Runnable>()
        val scheduler = NotificationAutoClearScheduler(
            postDelayed = { runnable, _ -> scheduled += runnable },
            removeCallbacks = { removed += it },
            clearNotifications = {}
        )

        scheduler.start()
        scheduler.stop()

        assertEquals(2, removed.size)
        assertSame(scheduled.single(), removed.last())
    }
}
