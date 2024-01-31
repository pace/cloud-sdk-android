package car.pace.cofu.ui.wallet

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultDialog
import car.pace.cofu.ui.component.DefaultListItem
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.component.dropShadow
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants.DEFAULT_LIST_ITEM_CONTENT_TYPE
import car.pace.cofu.util.Constants.SPACER_CONTENT_TYPE
import car.pace.cofu.util.Constants.SPACER_KEY
import car.pace.cofu.util.Constants.USER_HEADER_CONTENT_TYPE
import car.pace.cofu.util.Constants.USER_HEADER_KEY
import car.pace.cofu.util.JWTUtils
import car.pace.cofu.util.LogAndBreadcrumb
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.idkit.IDKit
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val email = remember {
        JWTUtils.getUserEMailFromToken(IDKit.cachedToken()).orEmpty()
    }
    val items = remember { viewModel.items }

    WalletScreenContent(
        email = email,
        items = items,
        onLogout = {
            LogAndBreadcrumb.i(LogAndBreadcrumb.WALLET, "User logged out")
            coroutineScope.launch {
                val activity = context.findActivity<AppCompatActivity>()
                viewModel.resetAppData(activity)
                onLogout()
            }
        },
        onNavigate = onNavigate
    )
}

@Composable
fun WalletScreenContent(
    email: String,
    items: List<Route>,
    onLogout: () -> Unit,
    onNavigate: (Route) -> Unit
) {
    Column {
        TextTopBar(
            text = stringResource(id = R.string.wallet_tab_label),
            backIcon = null
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            item(
                key = USER_HEADER_KEY,
                contentType = USER_HEADER_CONTENT_TYPE
            ) {
                UserHeader(
                    email = email,
                    onLogout = onLogout
                )
            }

            item(
                key = SPACER_KEY,
                contentType = SPACER_CONTENT_TYPE
            ) {
                Spacer(modifier = Modifier.padding(top = 28.dp))
            }

            items(
                items = items,
                key = Route::route,
                contentType = { DEFAULT_LIST_ITEM_CONTENT_TYPE }
            ) {
                val context = LocalContext.current

                DefaultListItem(
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClick = {
                            when (it) {
                                Route.TRANSACTIONS -> {
                                    LogAndBreadcrumb.d(LogAndBreadcrumb.WALLET, "Transactions gets displayed")
                                    AppKit.openTransactions(context)
                                }

                                Route.DELETE_ACCOUNT -> {
                                    LogAndBreadcrumb.d(LogAndBreadcrumb.WALLET, "PACE ID gets displayed")
                                    AppKit.openPaceID(context)
                                }

                                else -> onNavigate(it)
                            }
                        }
                    ),
                    icon = it.icon,
                    text = it.labelRes?.let { res -> stringResource(id = res) }.orEmpty()
                )
            }
        }
    }
}

@Composable
fun UserHeader(
    email: String,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .dropShadow()
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.surface
        )

        Column(
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                text = stringResource(id = R.string.wallet_header_text),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = email,
                modifier = Modifier.padding(top = 2.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(id = R.string.MENU_ITEMS_LOGOUT),
                modifier = Modifier
                    .clickable(
                        onClickLabel = stringResource(id = R.string.MENU_ITEMS_LOGOUT),
                        role = Role.Button,
                        onClick = { openDialog = true }
                    )
                    .padding(top = 2.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (openDialog) {
        LogoutDialog(
            onLogout = {
                openDialog = false
                onLogout()
            },
            onDismiss = {
                openDialog = false
            }
        )
    }
}

@Composable
fun LogoutDialog(
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    DefaultDialog(
        title = stringResource(id = R.string.DASHBOARD_LOGOUT_CONFIRM_TITLE),
        text = stringResource(id = R.string.DASHBOARD_LOGOUT_CONFIRM_DESCRIPTION),
        confirmButtonText = stringResource(id = R.string.DASHBOARD_LOGOUT_CONFIRM_ACTION_LOGOUT),
        dismissButtonText = stringResource(id = R.string.common_use_cancel),
        imageVector = Icons.Outlined.Logout,
        onConfirm = onLogout,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun WalletScreenContentPreview() {
    AppTheme {
        WalletScreenContent(
            email = "user@pace.car",
            items = listOf(Route.PAYMENT_METHODS, Route.TRANSACTIONS, Route.FUEL_TYPE),
            onLogout = {},
            onNavigate = {}
        )
    }
}

@Preview
@Composable
fun UserHeaderPreview() {
    AppTheme {
        UserHeader(
            email = "user@pace.car",
            modifier = Modifier.padding(20.dp),
            onLogout = {}
        )
    }
}

@Preview
@Composable
fun LogoutDialogPreview() {
    AppTheme {
        LogoutDialog(
            onLogout = {},
            onDismiss = {}
        )
    }
}
