package car.pace.cofu.ui.home

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import car.pace.cofu.ui.AppScaffold

@Composable
fun Home() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // TODO: Drawer content
                Text(text = "Drawer content")
            }
        }
    ) {
        AppScaffold(drawerState = drawerState) {

        }
    }
}
