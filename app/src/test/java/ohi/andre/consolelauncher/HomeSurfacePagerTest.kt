package ohi.andre.consolelauncher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeSurfacePagerTest {
    @Test
    fun horizontalSwipesMoveOnlyBetweenDashboardAndWallpaper() {
        assertEquals(
            HomeSurfacePager.WALLPAPER_PAGE,
            HomeSurfacePager.targetPage(HomeSurfacePager.DASHBOARD_PAGE, -900f, 40f)
        )
        assertEquals(
            HomeSurfacePager.DASHBOARD_PAGE,
            HomeSurfacePager.targetPage(HomeSurfacePager.WALLPAPER_PAGE, 900f, 40f)
        )
    }

    @Test
    fun wrongDirectionAndVerticalGesturesDoNotChangePages() {
        assertNull(HomeSurfacePager.targetPage(HomeSurfacePager.DASHBOARD_PAGE, 900f, 20f))
        assertNull(HomeSurfacePager.targetPage(HomeSurfacePager.WALLPAPER_PAGE, -900f, 20f))
        assertNull(HomeSurfacePager.targetPage(HomeSurfacePager.DASHBOARD_PAGE, -700f, 900f))
        assertNull(HomeSurfacePager.targetPage(HomeSurfacePager.DASHBOARD_PAGE, -300f, 10f))
    }

    @Test
    fun termuxWorkspaceIsNeverReachableByAHomeSwipe() {
        assertNull(HomeSurfacePager.targetPage(HomeSurfacePager.TERMUX_PAGE, -1200f, 0f))
        assertNull(HomeSurfacePager.targetPage(HomeSurfacePager.TERMUX_PAGE, 1200f, 0f))
        assertEquals(3, HomeSurfacePager.PAGE_COUNT)
    }
}
