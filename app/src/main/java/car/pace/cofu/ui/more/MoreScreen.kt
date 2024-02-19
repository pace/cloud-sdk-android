package car.pace.cofu.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultListItem
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants.DEFAULT_LIST_ITEM_CONTENT_TYPE
import car.pace.cofu.util.IntentUtils

@Composable
fun MoreScreen(
    viewModel: MoreViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit
) {
    Column {
        TextTopBar(
            text = stringResource(id = R.string.more_tab_label),
            backIcon = null
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            items(
                items = viewModel.items,
                key = MoreViewModel.MoreItem::id,
                contentType = { DEFAULT_LIST_ITEM_CONTENT_TYPE }
            ) {
                val context = LocalContext.current

                DefaultListItem(
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClick = {
                            if (it.route == Route.WEBSITE) {
                                val url = it.urlRes?.let { url -> context.getString(url) }
                                IntentUtils.launchInCustomTabIfAvailable(context, url)
                            } else {
                                onNavigate(it.route)
                            }
                        }
                    ),
                    icon = it.icon,
                    title = it.labelRes?.let { res -> stringResource(id = res) }.orEmpty()
                )
            }
        }
    }
}

@Preview
@Composable
fun MoreScreenPreview() {
    AppTheme {
        MoreScreen {}
    }
}
