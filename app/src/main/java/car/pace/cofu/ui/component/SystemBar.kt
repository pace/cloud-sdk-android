package car.pace.cofu.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.navigation.graph.BottomBarItem
import car.pace.cofu.ui.navigation.graph.Destination
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.Gray

@Composable
fun TopBar(currentDestination: String?) {
    when (currentDestination) {
        Destination.Onboarding.route, Destination.Home.List.route -> {
            LogoTopBar()
        }

        Destination.Wallet.List.route -> {
            TextTopBar(
                text = stringResource(id = BottomBarItem.WALLET.labelRes),
                backIcon = null
            )
        }

        Destination.More.List.route -> {
            TextTopBar(
                text = stringResource(id = BottomBarItem.MORE.labelRes),
                backIcon = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.ic_logo_connected_fueling_big),
                contentDescription = null
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextTopBar(
    text: String,
    backIcon: ImageVector? = Icons.Filled.ArrowBack,
    onBackPress: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = text,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )
        },
        navigationIcon = {
            if (backIcon != null) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = backIcon,
                        contentDescription = null
                    )
                }
            }
        }
    )
}

@Composable
fun BottomBar(
    destinations: Array<BottomBarItem>,
    currentDestination: String?,
    onNavigateToDestination: (BottomBarItem) -> Unit
) {
    Column {
        Divider()
        NavigationBar(tonalElevation = 0.dp) {
            destinations.forEach {
                NavigationBarItem(
                    selected = it.route == currentDestination,
                    onClick = { onNavigateToDestination(it) },
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = stringResource(id = it.labelRes)
                        )
                    },
                    label = {
                        Text(text = stringResource(id = it.labelRes))
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.surface,
                        unselectedIconColor = Gray,
                        unselectedTextColor = Gray
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun LogoTopBarPreview() {
    AppTheme {
        LogoTopBar()
    }
}

@Preview
@Composable
fun TextTopBarPreview() {
    AppTheme {
        TextTopBar(text = "Connected Fueling")
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    AppTheme {
        BottomBar(
            destinations = BottomBarItem.values(),
            currentDestination = BottomBarItem.HOME.route,
            onNavigateToDestination = {}
        )
    }
}
