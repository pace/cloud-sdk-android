package car.pace.cofu.ui.wallet.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultCircularProgressIndicator
import car.pace.cofu.ui.component.ErrorCard
import car.pace.cofu.ui.component.LoadingCard
import car.pace.cofu.ui.component.NoContentCard
import car.pace.cofu.ui.component.SmallErrorCard
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.wallet.preview.TransactionsProvider
import car.pace.cofu.util.BuildProvider
import cloud.pace.sdk.api.pay.generated.model.ReadOnlyLocation.Address
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.flow.flowOf

@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
    navigateToTransaction: (String) -> Unit
) {
    val showPrices = !BuildProvider.hidePrices()
    val transactions = viewModel.items.collectAsLazyPagingItems()

    TransactionsScreenContent(
        transactionItems = transactions,
        showPrices = showPrices,
        onTransactionItemClick = { navigateToTransaction(it.id) },
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun TransactionsScreenContent(
    transactionItems: LazyPagingItems<Transaction>,
    showPrices: Boolean,
    onTransactionItemClick: (Transaction) -> Unit,
    onNavigateUp: () -> Unit
) {
    Column {
        TextTopBar(
            text = stringResource(id = R.string.wallet_transactions_title),
            onNavigateUp = onNavigateUp
        )

        Column {
            when (transactionItems.loadState.refresh) {
                is LoadState.Loading -> {
                    LoadingCard(
                        title = stringResource(id = R.string.transactions_loading_title),
                        description = stringResource(id = R.string.transactions_loading_description),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                is LoadState.Error -> {
                    ErrorCard(
                        title = stringResource(id = R.string.general_error_title),
                        description = stringResource(id = R.string.transactions_error_description),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        buttonText = stringResource(id = R.string.common_use_retry),
                        onButtonClick = { transactionItems.retry() }
                    )
                }
                else -> TransactionsList(transactionItems = transactionItems, showPrices = showPrices, onTransactionItemClick = onTransactionItemClick)
            }
        }
    }
}

@Composable
fun TransactionsList(transactionItems: LazyPagingItems<Transaction>, showPrices: Boolean, modifier: Modifier = Modifier, onTransactionItemClick: (Transaction) -> Unit) {
    if (transactionItems.itemCount == 0) {
        NoContentCard(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            header = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = stringResource(id = R.string.transactions_empty_title),
            description = stringResource(id = R.string.transactions_empty_description)
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(count = transactionItems.itemCount) { index ->
            transactionItems[index]?.let { transaction ->
                TransactionsListItem(
                    modifier = Modifier.clickable { onTransactionItemClick(transaction) },
                    transaction = transaction,
                    isFirstItem = index == 0,
                    showPrices = showPrices
                )
            }
        }

        val appendState = transactionItems.loadState.append
        if (appendState is LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DefaultCircularProgressIndicator(
                        modifier = Modifier
                            .padding(vertical = 20.dp)
                            .size(30.dp)
                    )
                }
            }
        } else if (appendState is LoadState.Error) {
            item {
                SmallErrorCard(modifier = Modifier.padding(20.dp), title = stringResource(id = R.string.general_error_title))
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
fun TransactionsListItem(transaction: Transaction, isFirstItem: Boolean, showPrices: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(color = if (isFirstItem) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isFirstItem) {
                    Text(
                        text = stringResource(id = R.string.transactions_last_transaction_title),
                        modifier = Modifier.padding(bottom = 12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Text(
                    text = transaction.formatCreationDate(showWeekday = false),
                    modifier = Modifier.padding(bottom = 12.dp),
                    style = MaterialTheme.typography.titleSmall
                )

                Row {
                    Icon(
                        imageVector = Icons.Outlined.LocalGasStation,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = transaction.stationName ?: stringResource(id = R.string.gas_station_default_name),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (showPrices) {
                PriceCard(
                    formattedPrice = transaction.formatPrice(),
                    productName = transaction.productName
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp))
}

@Composable
private fun PriceCard(formattedPrice: String, productName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(100.dp)
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            modifier = Modifier.padding(bottom = 5.dp),
            text = productName,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = formattedPrice,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight(860))
        )
    }
}

@Preview
@Composable
private fun TransactionsScreenWithPricesPreview(@PreviewParameter(TransactionsProvider::class) pagingData: PagingData<Transaction>) {
    AppTheme {
        TransactionsScreenContent(
            transactionItems = flowOf(pagingData).collectAsLazyPagingItems(),
            showPrices = true,
            onTransactionItemClick = {},
            onNavigateUp = {}
        )
    }
}

@Preview
@Composable
private fun TransactionsScreenWithoutPricesPreview(@PreviewParameter(TransactionsProvider::class, limit = 1) pagingData: PagingData<Transaction>) {
    AppTheme {
        TransactionsScreenContent(
            transactionItems = flowOf(pagingData).collectAsLazyPagingItems(),
            showPrices = false,
            onTransactionItemClick = {},
            onNavigateUp = {}
        )
    }
}

internal fun createTransaction(
    createdAt: Date = Date(1649196000000),
    productName: String = "Super",
    price: Double = 23.76,
    currency: String = "EUR",
    stationName: String = "Gas what",
    address: Address? = Address().apply {
        street = "Fakestreet"
        houseNo = "123"
        postalCode = "76543"
        city = "Exampletown"
    },
    pumpNumber: Int? = 1,
    fuelAmount: Double = 16.54,
    pricePerFuelUnit: Double = 1.6798,
    fuelUnit: String = "Liter"
): Transaction = Transaction(
    id = UUID.randomUUID().toString(),
    createdAt = createdAt,
    productName = productName,
    price = price,
    currency = currency,
    stationName = stationName,
    address = address,
    pumpNumber = pumpNumber,
    fuelAmount = fuelAmount,
    pricePerFuelUnit = pricePerFuelUnit,
    fuelUnit = fuelUnit,
    paymentMethodId = UUID.randomUUID().toString()
)
