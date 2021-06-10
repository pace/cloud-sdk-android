package cloud.pace.sdk.utils

import cloud.pace.sdk.appkit.model.AppManifest
import kotlin.math.abs

object IconUtils {

    fun getBestMatchingIcon(requestedSize: Double, icons: Array<AppManifest.AppIcons>): AppManifest.AppIcons? {
        val pngIcons = icons.filter { it.type.contains("png") }.toTypedArray()
        val prefIcons = if (pngIcons.isEmpty()) icons else pngIcons

        return prefIcons
            .filter { appIcons ->
                appIcons.sizes
                    .split(" ")
                    .any {
                        val dimensions = it.split("x")
                        val width = dimensions.firstOrNull()?.toIntOrNull()
                        val height = dimensions.lastOrNull()?.toIntOrNull()

                        width != null && height != null
                    }
            }
            .minByOrNull { appIcons ->
                appIcons.sizes
                    .split(" ")
                    .mapNotNull {
                        val dimensions = it.split("x")
                        val width = dimensions.firstOrNull()?.toIntOrNull()
                        val height = dimensions.lastOrNull()?.toIntOrNull()

                        if (width != null && height != null) {
                            abs(requestedSize - width) + abs(requestedSize - height)
                        } else {
                            null
                        }
                    }
                    .minOrNull() ?: return null
            }
    }
}
