package car.pace.cofu.ui.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.component.DefaultListItem
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants
import car.pace.cofu.util.IntentUtils

@Composable
fun MoreScreen(
    viewModel: MoreViewModel = hiltViewModel(),
    onNavigate: (MenuItem) -> Unit
) {
    val menuItems = remember { viewModel.entries }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        items(
            items = menuItems,
            contentType = { Constants.DEFAULT_LIST_ITEM_CONTENT_TYPE }
        ) {
            val context = LocalContext.current

            val title = when (it.title) {
                is MenuTitle.TitleStringRes -> stringResource(id = it.title.titleRes)
                is MenuTitle.TitleString -> it.title.title
            }

            DefaultListItem(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = {
                        if (it.action is MenuItemAction.WebContent) {
                            IntentUtils.launchInCustomTabIfAvailable(context, it.action.url)
                        } else {
                            onNavigate(it)
                        }
                    }
                ),
                icon = ImageVector.vectorResource(id = it.icon),
                text = title
            )
        }
    }
}

@Preview
@Composable
fun MenuPreview() {
    AppTheme {
        MoreScreen {}
    }
}
