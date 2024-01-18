package car.pace.cofu.ui.more.licenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.PrimaryButtonText
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults

@Composable
fun LicensesScreen(
    onNavigateUp: () -> Unit
) {
    Column {
        TextTopBar(
            text = stringResource(id = R.string.MENU_ITEMS_LICENCES),
            onNavigateUp = onNavigateUp
        )

        LibrariesContainer(
            modifier = Modifier.fillMaxSize(),
            colors = LibraryDefaults.libraryColors(
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                badgeBackgroundColor = MaterialTheme.colorScheme.primary,
                badgeContentColor = PrimaryButtonText,
                dialogConfirmButtonColor = MaterialTheme.colorScheme.primary
            ),
            itemContentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
        )
    }
}

@Preview
@Composable
fun LicensesScreenPreview() {
    AppTheme {
        LicensesScreen {}
    }
}
