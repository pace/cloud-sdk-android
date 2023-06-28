package cloud.pace.sdk.utils

import cloud.pace.sdk.appkit.model.AppIcon
import kotlin.math.abs

object IconUtils {

    fun getBestMatchingIcon(requestedSize: Double, icons: List<AppIcon>): AppIcon? {
        return icons
            .filter { it.type?.contains("png") == true }
            .ifEmpty { icons }
            .filter { appIcons ->
                appIcons.sizes
                    ?.split(" ")
                    ?.any {
                        val dimensions = it.split("x")
                        val width = dimensions.firstOrNull()?.toIntOrNull()
                        val height = dimensions.lastOrNull()?.toIntOrNull()

                        width != null && height != null
                    } ?: false
            }
            .minByOrNull { appIcons ->
                appIcons.sizes
                    ?.split(" ")
                    ?.mapNotNull {
                        val dimensions = it.split("x")
                        val width = dimensions.firstOrNull()?.toIntOrNull()
                        val height = dimensions.lastOrNull()?.toIntOrNull()

                        if (width != null && height != null) {
                            abs(requestedSize - width) + abs(requestedSize - height)
                        } else {
                            null
                        }
                    }
                    ?.minOrNull() ?: return null
            }
    }
}
