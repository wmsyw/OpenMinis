package com.openminis.app.ui.chat

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [T-android-toolbar-gap] The last row sits the same distance above the
 * floating tool bar whether or not Tool preview draws its thumbnail.
 */
class ToolbarGapTest {

    private fun reserve(preview: Boolean) =
        FloatingToolBarMetrics.visibleTop(preview) + FloatingToolBarMetrics.ROW_GAP

    @Test fun `the gap above the overlay's visible top is the same in both states`() {
        assertEquals("preview ON keeps the reserve it always had (65 + 14)", 79.dp, reserve(true))
        assertEquals("preview OFF reserves for the 38dp bar, not the 65dp thumbnail", 52.dp, reserve(false))
        assertEquals(reserve(true) - FloatingToolBarMetrics.visibleTop(true), reserve(false) - FloatingToolBarMetrics.visibleTop(false))
        assertEquals("the difference is exactly the thumbnail overhang", 27.dp, reserve(true) - reserve(false))
    }

}
