package ohi.andre.consolelauncher

import org.junit.Assert.assertEquals
import org.junit.Test

class TerminalTrayGeometryTest {
    @Test
    fun topAlignedTrayKeepsItsTopEdgeWhenImeRaisesItsBottom() {
        val rootHeight = 2160
        val collapsedHeight = 132
        val imeOffset = 672
        val normalHeight = TerminalTrayGeometry.expandedHeight(
            rootHeight,
            collapsedHeight,
            keyboardVisible = false,
            imeBottomOffset = 0,
            topAligned = true
        )
        val keyboardHeight = TerminalTrayGeometry.expandedHeight(
            rootHeight,
            collapsedHeight,
            keyboardVisible = true,
            imeBottomOffset = imeOffset,
            topAligned = true
        )

        assertEquals(normalHeight - imeOffset, keyboardHeight)
        assertEquals(rootHeight - normalHeight, rootHeight - imeOffset - keyboardHeight)
    }

    @Test
    fun topAlignedTrayNeverShrinksBelowCollapsedHeight() {
        assertEquals(
            132,
            TerminalTrayGeometry.expandedHeight(
                rootHeight = 1000,
                collapsedHeight = 132,
                keyboardVisible = true,
                imeBottomOffset = 700,
                topAligned = true
            )
        )
    }

    @Test
    fun missingImeInsetUsesCompatibilityKeyboardFraction() {
        assertEquals(
            340,
            TerminalTrayGeometry.expandedHeight(
                rootHeight = 1000,
                collapsedHeight = 100,
                keyboardVisible = true,
                imeBottomOffset = 0,
                topAligned = true
            )
        )
    }

    @Test
    fun defaultBottomAlignedThemesKeepExistingFractions() {
        assertEquals(
            480,
            TerminalTrayGeometry.expandedHeight(1000, 100, false, 0, false)
        )
        assertEquals(
            340,
            TerminalTrayGeometry.expandedHeight(1000, 100, true, 600, false)
        )
    }
}
