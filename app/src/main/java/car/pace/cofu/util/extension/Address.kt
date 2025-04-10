package car.pace.cofu.util.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cloud.pace.sdk.api.pay.generated.model.ReadOnlyLocation
import cloud.pace.sdk.poikit.poi.Address

@Composable
fun Address.twoLineAddress() = remember {
    twoLineAddress(street, houseNumber, postalCode, city)
}

@Composable
fun Address.oneLineAddress() = remember {
    oneLineAddress(street, houseNumber, postalCode, city)
}

@Composable
fun ReadOnlyLocation.Address.twoLineAddress() = remember {
    twoLineAddress(street, houseNo, postalCode, city)
}

@Composable
fun ReadOnlyLocation.Address.oneLineAddress() = remember {
    oneLineAddress(street, houseNo, postalCode, city)
}

private fun twoLineAddress(street: String?, houseNumber: String?, postalCode: String?, city: String?): String {
    return if (street == null) {
        ""
    } else {
        val firstLineAddress = firstLineAddress(street, houseNumber)
        val secondLineAddress = secondLineAddress(street, postalCode, city)
        if (secondLineAddress.isEmpty()) firstLineAddress else "$firstLineAddress\n$secondLineAddress"
    }
}

private fun oneLineAddress(street: String?, houseNumber: String?, postalCode: String?, city: String?): String {
    return if (street == null) {
        ""
    } else {
        val firstLineAddress = firstLineAddress(street, houseNumber)
        val secondLineAddress = secondLineAddress(street, postalCode, city)
        if (secondLineAddress.isEmpty()) firstLineAddress else "$firstLineAddress, $secondLineAddress"
    }
}

private fun firstLineAddress(street: String?, houseNumber: String?): String {
    return if (street == null) {
        ""
    } else {
        if (houseNumber == null) street else "$street $houseNumber"
    }
}

private fun secondLineAddress(street: String?, postalCode: String?, city: String?): String {
    return if (street == null) {
        ""
    } else {
        if (city == null) "" else if (postalCode == null) city else "$postalCode $city"
    }
}
