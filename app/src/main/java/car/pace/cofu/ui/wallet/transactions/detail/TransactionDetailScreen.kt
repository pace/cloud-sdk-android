package car.pace.cofu.ui.wallet.transactions.detail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.ErrorCard
import car.pace.cofu.ui.component.LoadingCard
import car.pace.cofu.ui.component.Receipt
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.component.shape.ReceiptShape
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.BuildProvider
import car.pace.cofu.util.UiState
import car.pace.cofu.util.data
import car.pace.cofu.util.extension.PaymentMethodItem
import car.pace.cofu.util.extension.formatCreationDate
import car.pace.cofu.util.extension.formatFuelAmount
import car.pace.cofu.util.extension.formatPrice
import car.pace.cofu.util.extension.formatPricePerUnit
import car.pace.cofu.util.extension.oneLineAddress
import cloud.pace.sdk.api.pay.generated.model.Fuel
import cloud.pace.sdk.api.pay.generated.model.ReadOnlyLocation
import cloud.pace.sdk.api.pay.generated.model.Transaction
import java.util.Date
import java.util.UUID

@Composable
fun TransactionDetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val transaction by viewModel.transaction.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
    val receiptImage by viewModel.receiptImage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    TransactionDetailScreenContent(
        transaction = transaction,
        paymentMethod = paymentMethod,
        receiptImage = receiptImage,
        onNavigateUp = onNavigateUp,
        onRefresh = viewModel::refresh,
        onDownload = { viewModel.downloadTransaction(context) }
    )
}

@Composable
fun TransactionDetailScreenContent(
    transaction: UiState<Transaction>,
    paymentMethod: UiState<PaymentMethodItem>,
    receiptImage: UiState<Bitmap>,
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
    onDownload: () -> Unit
) {
    Column {
        TextTopBar(
            text = stringResource(R.string.transactions_details_title),
            onNavigateUp = onNavigateUp
        )

        if (transaction is UiState.Success && receiptImage is UiState.Success) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DetailsColumn(
                    transaction = transaction.data,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!BuildProvider.hidePrices()) {
                    PriceRow(
                        transaction = transaction.data,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, top = 20.dp, end = 20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                paymentMethod.data?.let {
                    Description(
                        text = it.kind.orEmpty(),
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    Description(
                        text = it.alias ?: it.kind.orEmpty(),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Receipt(
                    padBorderPattern = false,
                    modifier = Modifier
                        .padding(start = 20.dp, top = 24.dp, end = 20.dp)
                        .clickable(role = Role.Button, onClick = onDownload)
                ) {
                    Image(
                        bitmap = receiptImage.data.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            // we want to clip the image with the receipt shape, but using clip() directly has comically terrible performance on scroll events, so we render this directly into the image
                            .drawWithCache {
                                val receiptPath = ReceiptShape.createReceiptPath(size)
                                onDrawWithContent {
                                    clipPath(receiptPath) {
                                        this@onDrawWithContent.drawContent()
                                    }
                                }
                            }
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.transactions_tap_on_receipt_title),
                    modifier = Modifier.padding(top = 10.dp, bottom = 24.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        } else if (transaction is UiState.Loading || receiptImage is UiState.Loading) {
            LoadingCard(
                title = stringResource(id = R.string.transactions_details_loading_title),
                description = stringResource(id = R.string.transactions_details_loading_description),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        } else if (transaction is UiState.Error || receiptImage is UiState.Error) {
            ErrorCard(
                title = stringResource(id = R.string.general_error_title),
                description = stringResource(id = R.string.transactions_details_error_description),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                buttonText = stringResource(id = R.string.common_use_retry),
                onButtonClick = onRefresh
            )
        }
    }
}

@Composable
fun DetailsColumn(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.LocalGasStation,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        val brand = transaction.location?.brand
        Title(
            text = brand ?: stringResource(id = R.string.gas_station_default_name),
            modifier = Modifier.padding(top = 10.dp)
        )

        val address = transaction.location?.address
        if (address != null) {
            Description(
                text = address.oneLineAddress(),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val date = transaction.formatCreationDate()
        if (date != null) {
            Description(
                text = date,
                modifier = Modifier.padding(top = 20.dp)
            )
        }

        val pumpNo = transaction.fuel?.pumpNumber
        if (pumpNo != null) {
            Description(
                text = "${stringResource(R.string.common_use_pump)} $pumpNo",
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun PriceRow(
    transaction: Transaction,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            val productName = transaction.fuel?.productName
            val formattedFuelAmount = transaction.formatFuelAmount()
            val text = listOfNotNull(productName, formattedFuelAmount)
            if (text.isNotEmpty()) {
                Description(
                    text = text.joinToString(": "),
                    textAlign = TextAlign.Start
                )
            }

            val pricePerUnit = transaction.formatPricePerUnit()
            if (pricePerUnit != null) {
                Description(
                    text = pricePerUnit,
                    modifier = Modifier.padding(top = 2.dp),
                    textAlign = TextAlign.Start
                )
            }
        }

        val formattedPrice = transaction.formatPrice()
        if (formattedPrice != null) {
            Text(
                text = formattedPrice,
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Preview
@Composable
fun TransactionDetailScreenContentPreview() {
    AppTheme {
        TransactionDetailScreenContent(
            transaction = UiState.Success(
                Transaction().apply {
                    fuel = Fuel().apply {
                        amount = 25.85
                        pumpNumber = 1
                        pricePerUnit = 1.839
                        unit = "liter"
                    }
                    location = ReadOnlyLocation().apply {
                        brand = "Gas what"
                        address = ReadOnlyLocation.Address().apply {
                            street = "Fakestreet"
                            houseNo = "123"
                            postalCode = "76543"
                            city = "Exampletown"
                        }
                    }
                    createdAt = Date()
                    priceIncludingVAT = 47.53
                    currency = "EUR"
                }
            ),
            paymentMethod = UiState.Success(
                PaymentMethodItem(
                    id = UUID.randomUUID().toString(),
                    vendorId = UUID.randomUUID().toString(),
                    imageUrl = Uri.parse("https://example.com/paypal.png"),
                    kind = "paypal",
                    alias = "user@pace.car"
                )
            ),
            receiptImage = UiState.Success(
                BitmapFactory.decodeResource(
                    LocalContext.current.resources,
                    // random test image
                    R.drawable.ic_map_loading_large
                )
            ),
            onNavigateUp = {},
            onRefresh = {},
            onDownload = {}
        )
    }
}

@Preview
@Composable
fun DetailsColumnPreview() {
    AppTheme {
        DetailsColumn(
            transaction = Transaction().apply {
                fuel = Fuel().apply {
                    pumpNumber = 1
                }
                location = ReadOnlyLocation().apply {
                    brand = "Gas what"
                    address = ReadOnlyLocation.Address().apply {
                        street = "Fakestreet"
                        houseNo = "123"
                        postalCode = "76543"
                        city = "Exampletown"
                    }
                }
                createdAt = Date()
            }
        )
    }
}

@Preview
@Composable
fun PriceRowPreview() {
    AppTheme {
        PriceRow(
            transaction = Transaction().apply {
                fuel = Fuel().apply {
                    amount = 25.85
                    pumpNumber = 1
                    pricePerUnit = 1.839
                    unit = "liter"
                }
                priceIncludingVAT = 47.53
                currency = "EUR"
            }
        )
    }
}
