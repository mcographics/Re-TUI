package ohi.andre.consolelauncher

import kotlin.math.abs

internal object HomeSurfacePager {
    const val DASHBOARD_PAGE = 0
    const val WALLPAPER_PAGE = 1
    const val TERMUX_PAGE = 2
    const val PAGE_COUNT = 3

    private const val MIN_HORIZONTAL_VELOCITY = 450f
    private const val HORIZONTAL_DOMINANCE = 1.2f

    fun targetPage(currentPage: Int, velocityX: Float, velocityY: Float): Int? {
        val horizontalSpeed = abs(velocityX)
        if (horizontalSpeed < MIN_HORIZONTAL_VELOCITY ||
            horizontalSpeed <= abs(velocityY) * HORIZONTAL_DOMINANCE
        ) {
            return null
        }

        return when {
            currentPage == DASHBOARD_PAGE && velocityX < 0f -> WALLPAPER_PAGE
            currentPage == WALLPAPER_PAGE && velocityX > 0f -> DASHBOARD_PAGE
            else -> null
        }
    }
}
