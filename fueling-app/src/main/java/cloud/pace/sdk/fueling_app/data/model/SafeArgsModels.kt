package cloud.pace.sdk.fueling_app.data.model

import android.os.Parcelable
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class GasStation(val id: String, val name: String?, val address: String?, val currency: String?) : Parcelable

@Parcelize
data class Pump(val id: String, val identifier: String?) : Parcelable

@Parcelize
data class PaymentMethod(val id: String, val kind: String?, val alias: String?, val identificationString: String?, val twoFactor: Boolean, val merchantName: String?) : Parcelable

@Parcelize
data class PumpResponse(
    val id: String,
    val identifier: String?,
    val fuelingProcess: PumpResponse.FuelingProcess?,
    val productName: String?,
    val fuelAmount: Double?,
    val pricePerUnit: Double?,
    var priceIncludingVAT: Double?
) : Parcelable
