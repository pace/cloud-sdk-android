package cloud.pace.sdk.fueling_app.util

import android.content.Context
import cloud.pace.sdk.api.fueling.generated.model.PaymentMethod
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.util.Constants.AMEX
import cloud.pace.sdk.fueling_app.util.Constants.CREDITCARD
import cloud.pace.sdk.fueling_app.util.Constants.DINERSCLUB
import cloud.pace.sdk.fueling_app.util.Constants.DINERS_CLUB
import cloud.pace.sdk.fueling_app.util.Constants.DKV
import cloud.pace.sdk.fueling_app.util.Constants.GIROPAY
import cloud.pace.sdk.fueling_app.util.Constants.HOYER
import cloud.pace.sdk.fueling_app.util.Constants.HOYERCARD
import cloud.pace.sdk.fueling_app.util.Constants.JCB
import cloud.pace.sdk.fueling_app.util.Constants.MAESTRO
import cloud.pace.sdk.fueling_app.util.Constants.MASTERCARD
import cloud.pace.sdk.fueling_app.util.Constants.PAYDIREKT
import cloud.pace.sdk.fueling_app.util.Constants.PAYPAL
import cloud.pace.sdk.fueling_app.util.Constants.SEPA
import cloud.pace.sdk.fueling_app.util.Constants.VISA
import cloud.pace.sdk.fueling_app.util.Constants.ZGM
import cloud.pace.sdk.poikit.poi.GasStation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

val GasStation.addressOneLine: String?
    get() {
        val street = address?.street
        val houseNumber = address?.houseNumber
        val postalCode = address?.postalCode
        val city = address?.city

        return if (street == null) {
            null
        } else {
            val addressFirstSubstring = if (houseNumber == null) street else "$street $houseNumber"
            val addressSecondSubstring = if (city == null) null else if (postalCode == null) city else "$postalCode $city"

            if (addressSecondSubstring.isNullOrEmpty()) addressFirstSubstring else "$addressFirstSubstring, $addressSecondSubstring"
        }
    }

val GasStation.addressTwoLines: String?
    get() {
        val address = address
        return if (address?.street == null) {
            null
        } else {
            val addressFirstSubstring = if (address.houseNumber == null) address.street else "${address.street} ${address.houseNumber}"
            val addressSecondSubstring = if (address.city == null) null else if (address.postalCode == null) address.city else "${address.postalCode} ${address.city}"
            if (addressSecondSubstring.isNullOrEmpty()) addressFirstSubstring else "$addressFirstSubstring\n$addressSecondSubstring"
        }
    }

fun GasStation.asSafeArgsGasStation(): cloud.pace.sdk.fueling_app.data.model.GasStation {
    return cloud.pace.sdk.fueling_app.data.model.GasStation(id, name, addressOneLine, currency)
}

fun PaymentMethod.asSafeArgsPaymentMethod(): cloud.pace.sdk.fueling_app.data.model.PaymentMethod {
    val paymentMethodMetaAdapter = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        .adapter(PaymentMethod.Meta::class.java)
    val merchantName = (getMeta()?.get<PaymentMethod.Meta>(paymentMethodMetaAdapter) as? PaymentMethod.Meta)?.merchantName

    return cloud.pace.sdk.fueling_app.data.model.PaymentMethod(id, kind, alias, identificationString, twoFactor == true, merchantName)
}

fun PumpResponse.asSafeArgsPumpResponse(): cloud.pace.sdk.fueling_app.data.model.PumpResponse {
    return cloud.pace.sdk.fueling_app.data.model.PumpResponse(id, identifier, fuelingProcess, productName, fuelAmount, pricePerUnit, priceIncludingVAT)
}

fun cloud.pace.sdk.fueling_app.data.model.PaymentMethod.localizedKind(context: Context): String? {
    return when (kind) {
        AMEX, CREDITCARD, DINERSCLUB, DINERS_CLUB, MAESTRO, MASTERCARD, VISA -> context.getString(R.string.payment_method_kind_credit_card)
        DKV -> context.getString(R.string.payment_method_kind_dkv)
        HOYER, HOYERCARD -> context.getString(R.string.payment_method_kind_hoyer)
        JCB -> context.getString(R.string.payment_method_kind_fuel_card)
        GIROPAY, PAYDIREKT -> context.getString(R.string.payment_method_kind_giropay)
        PAYPAL -> context.getString(R.string.payment_method_kind_paypal)
        SEPA -> context.getString(R.string.payment_method_kind_sepa)
        ZGM -> context.getString(R.string.payment_method_kind_zgm)
        else -> kind?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
