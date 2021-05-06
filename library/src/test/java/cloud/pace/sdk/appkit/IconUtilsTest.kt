package cloud.pace.sdk.appkit

import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.utils.IconUtils
import junit.framework.Assert.assertEquals
import org.junit.Test

class IconUtilsTest {

    @Test
    fun `get png icon`() {
        val buttonWidth = 80.0
        val pngIcon = AppManifest.AppIcons("logo.png", "12x12 50x50 120x120", "image/png")
        val favIcon = AppManifest.AppIcons("favicon.ico", "64x64 32x32 24x24 16x16", "image/x-icon")

        val bestIcon = IconUtils.getBestMatchingIcon(buttonWidth, arrayOf(pngIcon, favIcon))
        assertEquals(pngIcon.src, bestIcon?.src)
        assertEquals(pngIcon.type, bestIcon?.type)
    }

    @Test
    fun `get best png icon`() {
        val buttonWidth = 80.0
        val smallPngIcon = AppManifest.AppIcons("smallLogo.png", "12x12 30x30", "image/png")
        val pngIcon = AppManifest.AppIcons("logo.png", "50x50 120x120", "image/png")
        val favIcon = AppManifest.AppIcons("favicon.ico", "64x64 32x32 24x24 16x16", "image/x-icon")

        val bestIcon = IconUtils.getBestMatchingIcon(buttonWidth, arrayOf(smallPngIcon, pngIcon, favIcon))
        assertEquals(pngIcon.src, bestIcon?.src)
        assertEquals(pngIcon.type, bestIcon?.type)
    }

    @Test
    fun `only favicon available`() {
        val buttonWidth = 80.0
        val favIcon = AppManifest.AppIcons("favicon.ico", "64x64 32x32 24x24 16x16", "image/x-icon")

        val bestIcon = IconUtils.getBestMatchingIcon(buttonWidth, arrayOf(favIcon))
        assertEquals(favIcon.src, bestIcon?.src)
        assertEquals(favIcon.type, bestIcon?.type)
    }
}
