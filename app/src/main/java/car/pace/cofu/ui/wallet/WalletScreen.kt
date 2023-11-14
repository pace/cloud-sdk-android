package car.pace.cofu.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.component.DEFAULT_LIST_ITEM_CONTENT_TYPE
import car.pace.cofu.ui.component.DefaultListItem
import car.pace.cofu.ui.component.ListItem
import car.pace.cofu.ui.component.dropShadow
import car.pace.cofu.ui.navigation.graph.Destination
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.DarkGray
import car.pace.cofu.ui.theme.Gray
import car.pace.cofu.ui.theme.LightGray

private const val USER_HEADER_KEY = "UserHeader"
private const val USER_HEADER_CONTENT_TYPE = "UserHeader"
private const val SPACER_KEY = "Spacer"
private const val SPACER_CONTENT_TYPE = "Spacer"

@Composable
fun WalletScreen(
    onNavigate: (String) -> Unit
) {
    val items = remember {
        listOf(
            ListItem(Destination.Wallet.Methods.route, Icons.Outlined.AccountBalanceWallet, R.string.methods_list_item),
            ListItem(Destination.Wallet.Transactions.route, Icons.Outlined.ReceiptLong, R.string.transactions_list_item),
            ListItem(Destination.Wallet.FuelType.route, Icons.Outlined.LocalGasStation, R.string.fuel_type_list_item)
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        item(
            key = USER_HEADER_KEY,
            contentType = USER_HEADER_CONTENT_TYPE
        ) {
            // TODO: email
            UserHeader(email = "melissa@pace.car")
        }

        item(
            key = SPACER_KEY,
            contentType = SPACER_CONTENT_TYPE
        ) {
            Spacer(modifier = Modifier.padding(top = 30.dp))
        }

        items(
            items = items,
            key = ListItem::id,
            contentType = { DEFAULT_LIST_ITEM_CONTENT_TYPE }
        ) {
            DefaultListItem(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = { onNavigate(it.id) }
                ),
                icon = it.icon,
                text = stringResource(id = it.textRes)
            )
        }
    }
}

@Composable
fun UserHeader(email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dropShadow(
                color = Color.Gray.copy(alpha = 0.3f),
                blurRadius = 10.dp
            )
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = LightGray
        )

        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.wallet_user_header_text),
                color = Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = email,
                modifier = Modifier.padding(top = 2.dp),
                color = DarkGray,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight(510)
                )
            )
            Text(
                text = stringResource(id = R.string.MENU_ITEMS_LOGOUT),
                modifier = Modifier
                    .clickable(
                        onClickLabel = stringResource(id = R.string.MENU_ITEMS_LOGOUT),
                        role = Role.Button
                    ) {
                        // TODO: logout
                    }
                    .padding(top = 2.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight(510)
                )
            )
        }
    }
}

@Preview
@Composable
fun WalletScreenPreview() {
    AppTheme {
        WalletScreen {}
    }
}

@Preview
@Composable
fun UserHeaderPreview() {
    AppTheme {
        UserHeader(email = "user@pace.car")
    }
}
