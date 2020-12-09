package cloud.pace.sdk.paykit.transactions

import com.squareup.moshi.Json
import java.util.*

data class TransactionsResponse(val data: List<Transaction>)

data class Transaction(
    val type: String?,
    val id: String?,
    val links: TransactionLinks?,
    val attributes: TransactionAttributes?
)

data class TransactionLinks(
    val receipt: TransactionReceipt?,
    @Json(name = "receiptPDF")
    val receiptPdf: TransactionReceipt?
)

data class TransactionReceipt(
    val href: String?,
    val meta: TransactionMeta?
)

data class TransactionMeta(val mimeType: TransactionMimeType?)

enum class TransactionMimeType(val value: String) {
    @Json(name = "image/png")
    IMAGE_PNG("image/png"),

    @Json(name = "application/pdf")
    APPLICATION_PDF("application/pdf")
}

data class TransactionAttributes(
    val createdAt: Date?,
    val updatedAt: Date?,
    val paymentMethodKind: String?,
    val paymentMethodId: String?,
    val paymentToken: String?,
    @Json(name = "purposePRN")
    val purposePrn: String?,
    @Json(name = "providerPRN")
    val providerPrn: String?,
    @Json(name = "issuerPRN")
    val issuerPrn: String?,
    val vin: String?,
    val mileage: Int?,
    @Json(name = "priceIncludingVAT")
    val priceIncludingVat: Double?,
    @Json(name = "priceWithoutVAT")
    val priceWithoutVat: Double?,
    val fuel: TransactionFuel?,
    val currency: String?,
    @Json(name = "VAT")
    val vat: TransactionVat?,
    val references: List<String>?,
    val location: TransactionLocation?
)

data class TransactionVat(
    val amount: Double?,
    val rate: Double?
)

data class TransactionFuel(
    val pumpNumber: Int?,
    val unit: String?,
    val pricePerUnit: Double?,
    val amount: Double?,
    val productName: String?,
    val type: String?
)

data class TransactionLocation(
    val latitude: Double?,
    val longitude: Double?,
    val brand: String?,
    val address: TransactionAddress?
)

data class TransactionAddress(
    val street: String?,
    val houseNo: String?,
    val postalCode: String?,
    val city: String?,
    val countryCode: String?
)
