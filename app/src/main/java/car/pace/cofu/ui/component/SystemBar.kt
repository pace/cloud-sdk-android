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
import car.pace.cofu.ui.navigation.graph.Graph
import car.pace.cofu.ui.navigation.graph.bottomBarGraphs
import car.pace.cofu.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogoTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.ic_brand_logo),
                contentDescription = null
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextTopBar(
    text: String? = null,
    backIcon: ImageVector? = Icons.Filled.ArrowBack,
    onNavigateUp: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            if (text != null) {
                Text(
                    text = text,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        navigationIcon = {
            if (backIcon != null) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = backIcon,
                        contentDescription = null
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun BottomBar(
    destinations: List<Graph>,
    currentGraph: Graph?,
    onNavigateToGraph: (Graph) -> Unit
) {
    Column {
        Divider()
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp
        ) {
            destinations.forEach {
                NavigationBarItem(
                    selected = it == currentGraph,
                    onClick = { onNavigateToGraph(it) },
                    icon = {
                        val icon = it.icon
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = it.labelRes?.let { res -> stringResource(id = res) }
                            )
                        }
                    },
                    label = {
                        Text(text = it.labelRes?.let { res -> stringResource(id = res) }.orEmpty())
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
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
            destinations = bottomBarGraphs,
            currentGraph = Graph.WALLET,
            onNavigateToGraph = {}
        )
    }
}
