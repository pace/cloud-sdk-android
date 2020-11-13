package cloud.pace.sdk.utils

import cloud.pace.sdk.appkit.model.AppManifest

object IconUtils {

    fun getBestMatchingIcon(buttonWidth: Double, icons: Array<AppManifest.AppIcons>): AppManifest.AppIcons? {
        val sizeDiffs = mutableListOf<Pair<AppManifest.AppIcons, Double>>()
        icons.forEach { icon ->
            try {
                val iconSizes = icon.sizes.split("\\s+".toRegex())
                iconSizes.forEach {
                    val iconSize = it.split("x").first().toInt()
                    sizeDiffs.add(Pair(icon, iconSize - buttonWidth))
                }
            } catch (e: NumberFormatException) {
            }
        }

        val pngIcons = sizeDiffs.filter { it.first.type.contains("png") }
        var bestSizePNGIcon = pngIcons.filter { it.second >= 0 }.minByOrNull { it.second }

        if (bestSizePNGIcon == null) {
            bestSizePNGIcon = pngIcons.maxByOrNull { it.second }
        }

        return bestSizePNGIcon?.first ?: sizeDiffs.minByOrNull { it.second }?.first
    }
}
