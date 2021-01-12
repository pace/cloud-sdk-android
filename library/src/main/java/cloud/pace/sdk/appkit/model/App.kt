package cloud.pace.sdk.appkit.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class App(
    var name: String,
    var shortName: String,
    var description: String? = null,
    var url: String,
    var logo: Bitmap? = null,
    var iconBackgroundColor: String? = null,
    var textBackgroundColor: String? = null,
    var textColor: String? = null,
    var display: String? = null,
    var gasStationId: String? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        return when {
            (other is App) -> {
                url == other.url
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }
}
