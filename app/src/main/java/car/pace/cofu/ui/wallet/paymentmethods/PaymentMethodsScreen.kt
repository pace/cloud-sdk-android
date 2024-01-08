package car.pace.cofu.ui.wallet.paymentmethods

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.R
import car.pace.cofu.ui.component.ErrorCard
import car.pace.cofu.ui.component.LoadingCard
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.component.forwardingPainter
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants.PAYMENT_METHOD_LIST_ITEM_CONTENT_TYPE
import car.pace.cofu.util.UiState
import car.pace.cofu.util.extension.PaymentMethodItem
import car.pace.cofu.util.extension.name
import cloud.pace.sdk.appkit.AppKit
import coil.compose.AsyncImage
import java.util.UUID

@Composable
fun PaymentMethodsScreen(
    viewModel: PaymentMethodsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    PaymentMethodsScreenContent(
        uiState = uiState,
        onItemClick = {
            AppKit.openAppActivity(context, viewModel.paymentMethodUrl(it.id), true)
        },
        onAddClick = {
            AppKit.openAppActivity(context, viewModel.paymentMethodCreateUrl(), true)
        },
        onRefresh = viewModel::refresh
    )
}

@Composable
fun PaymentMethodsScreenContent(
    uiState: UiState<List<PaymentMethodItem>>,
    onItemClick: (PaymentMethodItem) -> Unit,
    onAddClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        when (uiState) {
            is UiState.Loading -> {
                LoadingCard(
                    title = stringResource(id = R.string.payment_methods_loading_title),
                    description = stringResource(id = R.string.payment_methods_loading_description)
                )
            }

            is UiState.Success -> {
                val items = uiState.data
                if (items.isEmpty()) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        ErrorCard(
                            title = stringResource(id = R.string.payment_methods_empty_title),
                            description = stringResource(id = R.string.payment_methods_empty_description),
                            imageVector = Icons.Outlined.CreditCard
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = items,
                            key = PaymentMethodItem::id,
                            contentType = { PAYMENT_METHOD_LIST_ITEM_CONTENT_TYPE }
                        ) {
                            PaymentMethodListItem(
                                modifier = Modifier.clickable(
                                    role = Role.Button,
                                    onClick = { onItemClick(it) }
                                ),
                                imageUrl = it.imageUrl,
                                kind = it.kind,
                                alias = it.alias
                            )
                        }
                    }
                }

                SecondaryButton(
                    text = stringResource(id = R.string.payment_methods_add_button),
                    onClick = onAddClick
                )
            }

            is UiState.Error -> {
                ErrorCard(
                    title = stringResource(id = R.string.general_error_title),
                    description = stringResource(id = R.string.payment_methods_error_description),
                    buttonText = stringResource(id = R.string.common_use_retry),
                    onButtonClick = onRefresh
                )
            }
        }
    }
}

@Composable
fun PaymentMethodListItem(
    modifier: Modifier = Modifier,
    imageUrl: Uri?,
    kind: String?,
    alias: String?
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val name = remember(kind) {
                name(context, kind)
            }
            val fallbackIconPainter = forwardingPainter(
                painter = rememberVectorPainter(Icons.Outlined.CreditCard),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )

            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.size(32.dp),
                placeholder = fallbackIconPainter,
                error = fallbackIconPainter
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = alias ?: name,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Divider()
    }
}

@Preview
@Composable
fun PaymentMethodsScreenContentPreview() {
    AppTheme {
        PaymentMethodsScreenContent(
            uiState = UiState.Success(
                listOf(
                    PaymentMethodItem(
                        id = UUID.randomUUID().toString(),
                        vendorId = UUID.randomUUID().toString(),
                        imageUrl = Uri.parse("https://example.com/paypal.png"),
                        kind = "paypal",
                        alias = "user@pace.car"
                    ),
                    PaymentMethodItem(
                        id = UUID.randomUUID().toString(),
                        vendorId = UUID.randomUUID().toString(),
                        imageUrl = Uri.parse("https://example.com/giropay.png"),
                        kind = "giropay",
                        alias = "giropay"
                    ),
                    PaymentMethodItem(
                        id = UUID.randomUUID().toString(),
                        vendorId = UUID.randomUUID().toString(),
                        imageUrl = Uri.parse("https://example.com/visa.png"),
                        kind = "creditcard",
                        alias = "Visa card"
                    )
                )
            ),
            onItemClick = {},
            onAddClick = {},
            onRefresh = {}
        )
    }
}

@Preview
@Composable
fun MethodListItemPreview() {
    AppTheme {
        PaymentMethodListItem(
            imageUrl = Uri.parse("https://example.com/paypal.png"),
            kind = "PayPal",
            alias = "user@pace.car"
        )
    }
}
