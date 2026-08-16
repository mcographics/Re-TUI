package ohi.andre.consolelauncher

import kotlin.math.max

/** Pure sizing policy for the terminal output tray. */
internal object TerminalTrayGeometry {
    private const val TOP_ALIGNED_TRAY_FRACTION = 0.52f
    private const val DEFAULT_TRAY_FRACTION = 0.48f
    private const val KEYBOARD_TRAY_FRACTION = 0.34f

    fun expandedHeight(
        rootHeight: Int,
        collapsedHeight: Int,
        keyboardVisible: Boolean,
        imeBottomOffset: Int,
        topAligned: Boolean
    ): Int {
        val safeRootHeight = max(0, rootHeight)
        val safeCollapsedHeight = max(0, collapsedHeight)
        if (safeRootHeight == 0) {
            return safeCollapsedHeight
        }

        val preferredHeight = if (topAligned) {
            val normalHeight = Math.round(safeRootHeight * TOP_ALIGNED_TRAY_FRACTION)
            if (keyboardVisible && imeBottomOffset > 0) {
                // The tray is already raised by the IME inset. Remove that same amount
                // from its height so its top edge remains at the no-keyboard position.
                normalHeight - imeBottomOffset
            } else if (keyboardVisible) {
                // Compatibility fallback for devices that report IME visibility without
                // an inset measurement.
                Math.round(safeRootHeight * KEYBOARD_TRAY_FRACTION)
            } else {
                normalHeight
            }
        } else {
            Math.round(
                safeRootHeight *
                    if (keyboardVisible) KEYBOARD_TRAY_FRACTION else DEFAULT_TRAY_FRACTION
            )
        }

        return max(safeCollapsedHeight, preferredHeight)
    }
}
