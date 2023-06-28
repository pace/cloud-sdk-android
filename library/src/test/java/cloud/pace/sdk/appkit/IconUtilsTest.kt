package cloud.pace.sdk.appkit

import cloud.pace.sdk.appkit.model.AppIcon
import cloud.pace.sdk.utils.IconUtils
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Test

class IconUtilsTest {

    private val requestedSize = 64.0

    @Test
    fun `get best png icon from multiple icons`() {
        val smallPngIcon = AppIcon("smallLogo.png", "12x12 30x30", "image/png")
        val appIcon1 = AppIcon("logo1.png", "50x50 120x120", "image/png")
        val appIcon2 = AppIcon("logo2.png", "12x12 16x21 65x63", "image/png")
        val appIcon3 = AppIcon("logo3.png", "120x120 128x64", "image/png")
        val appIcon4 = AppIcon("logo4.png", "45x67 32x32", "image/png")
        val appIcon5 = AppIcon("logo5.png", "asdf", "image/png")
        val favIcon = AppIcon("favicon.ico", "64x64 32x32 24x24 16x16", "image/x-icon")

        val bestIcon = IconUtils.getBestMatchingIcon(requestedSize, listOf(smallPngIcon, appIcon1, appIcon2, appIcon3, appIcon4, appIcon5, favIcon))
        assertEquals(appIcon2, bestIcon)
    }

    @Test
    fun `only favicon available`() {
        val favIcon = AppIcon("favicon.ico", "64x64 32x32 24x24 16x16", "image/x-icon")

        val bestIcon = IconUtils.getBestMatchingIcon(requestedSize, listOf(favIcon))
        assertEquals(favIcon, bestIcon)
    }

    @Test
    fun `only one png icon available`() {
        val appIcon = AppIcon("logo.png", "32x32", "image/png")
        val selectedIcon = IconUtils.getBestMatchingIcon(requestedSize, listOf(appIcon))
        assertEquals(appIcon, selectedIcon)
    }

    @Test
    fun `no icon available`() {
        val selectedIcon = IconUtils.getBestMatchingIcon(requestedSize, emptyList())
        assertNull(selectedIcon)
    }

    @Test
    fun `no icon available because of missing sizes for one icon`() {
        val appIcon = AppIcon("logo.png", "", "image/png")
        val selectedIcon = IconUtils.getBestMatchingIcon(requestedSize, listOf(appIcon))
        assertNull(selectedIcon)
    }

    @Test
    fun `no icon available because of missing sizes for all icons`() {
        val appIcon1 = AppIcon("logo1.png", "", "image/png")
        val appIcon2 = AppIcon("logo2.png", "", "image/png")
        val selectedIcon = IconUtils.getBestMatchingIcon(requestedSize, listOf(appIcon1, appIcon2))
        assertNull(selectedIcon)
    }
}
