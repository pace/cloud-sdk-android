package cloud.pace.sdk.app.ui.components

import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cloud.pace.sdk.api.pay.generated.model.Transactions
import cloud.pace.sdk.app.MainScreenActivity
import cloud.pace.sdk.app.R
import cloud.pace.sdk.app.biometryStatus
import cloud.pace.sdk.app.ui.components.dashboardscreen.DashboardScreen
import cloud.pace.sdk.app.ui.components.listscreen.ListScreen
import cloud.pace.sdk.app.ui.components.settings.SettingsScreen
import cloud.pace.sdk.app.ui.theme.Screen
import cloud.pace.sdk.app.ui.theme.customShape
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.app.drawer.ui.AppDrawerHost
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.coroutines.launch

@Composable
fun App(
    activity: MainScreenActivity,
    lifecycleScope: LifecycleCoroutineScope,
    transactions: Transactions,
    gasStations: List<GasStation>,
    location: Location?,
    permissionsGranted: Boolean
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colors.primarySurface)
            .safeDrawingPadding(),
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                scaffoldState.drawerState.open()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Rounded.Menu, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(navController)
        },
        drawerContent = {
            Drawer()
        },
        drawerShape = customShape()
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.List.route
            ) {
                composable(Screen.List.route) {
                    ListScreen(gasStations, location, permissionsGranted)
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(transactions) {
                        scope.launch {
                            AppKit.openPaymentApp(activity, true)
                        }
                    }
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(activity, lifecycleScope) {
                        activity.finish()
                    }
                }
            }

            AppDrawerHost()

            // Dialog that asks the user if he wants to activate the BioAuth, if he agrees the BioAuth will be enabled.
            if (!biometryStatus.value) {
                SuccessfulLoginDialog {
                    IDKit.enableBiometricAuthentication {
                        when (it) {
                            is Success -> Toast.makeText(activity, if (it.result) "Biometric authentication set" else "Biometric authentication not set", Toast.LENGTH_SHORT).show()
                            is Failure -> Toast.makeText(activity, it.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Drawer() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var textValue by remember { mutableStateOf(TextFieldValue("")) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset(10.dp, 80.dp)
    ) {
        Column(
            modifier = Modifier
                .offset(0.dp, 60.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.size(180.dp, 56.dp),
                value = textValue,
                onValueChange = { textValue = it },
                label = { Text("PoiID") },
                textStyle = MaterialTheme.typography.body2
            )

            OutlinedButton(
                modifier = Modifier
                    .size(180.dp, 60.dp)
                    .padding(0.dp, 10.dp),
                onClick = {
                    val poiId = textValue.text
                    if (poiId.isBlank()) {
                        Toast.makeText(context, "POI ID must not be empty", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Pressed is POI in range ? button", Toast.LENGTH_LONG).show()
                        val start = System.currentTimeMillis()
                        coroutineScope.launch {
                            val isPoiInRange = POIKit.isPoiInRange(poiId)
                            val elapsedTime = System.currentTimeMillis() - start
                            Toast.makeText(context, "Is POI in range result is $isPoiInRange and took $elapsedTime ms", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            ) {
                Text("Is POI in range?")
            }
        }
    }
}
