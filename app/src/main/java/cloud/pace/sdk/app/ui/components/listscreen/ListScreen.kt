package cloud.pace.sdk.app.ui.components.listscreen

import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import cloud.pace.sdk.app.R
import cloud.pace.sdk.app.ui.theme.Screen
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import kotlinx.coroutines.launch

@Composable
fun ListScreen(gasStationList: List<GasStation>, location: LiveData<Location>, permissionGranted: LiveData<Boolean?>) {
    val newLocation by location.observeAsState()
    val isPermissionGranted by permissionGranted.observeAsState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    Scaffold(
        scaffoldState = scaffoldState,
        drawerContent = {
            ListScreenDrawer()
        },
        drawerShape = customShape(),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Divider(
                    color = Color.Black,
                    thickness = 3.dp,
                )
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                ) {
                    if (isPermissionGranted == false) {
                        Snackbar {
                            Text("Functions that require access to the fine location of this device do not work currently. You must first grant the permission if you want to use these functions!")
                        }
                    }
                    LoadListData(gasStationList, newLocation)
                }
            }
        },
        topBar = {
            IconButton(
                onClick =
                {
                    scope.launch {
                        scaffoldState.drawerState.open()
                    }
                },
                modifier = Modifier.padding(11.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_menu_24),
                    contentDescription = "",
                    modifier = Modifier
                        .size(36.dp),
                )
            }
            Text(
                text = Screen.List.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.primary,
                fontSize = 26.sp,
                modifier = Modifier
                    .offset(120.dp, 16.dp)
            )
        }
    )
}

/**
 * Drawer that can be opened on the listScreen
 *
 */

@Composable
fun ListScreenDrawer() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var textValue by remember { mutableStateOf(TextFieldValue("PoiID")) }
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
                modifier = Modifier
                    .size(180.dp, 50.dp),
                value = textValue,
                onValueChange = { textValue = it }
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
                Text("Is POI in range ?")
            }
        }
    }
}

@Composable
fun LoadListData(gasStationList: List<GasStation>, location: Location?) {
    gasStationList.forEach {
        ListScreenListItem(it, location)

        Divider(
            color = Color.Black,
            thickness = 2.dp,
            modifier = Modifier.padding(6.dp)
        )
    }
    Divider(
        color = Color.Black,
        thickness = 0.dp,
        modifier = Modifier.padding(30.dp)
    )
}

// Shape of the Drawer
fun customShape() = object : Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cR = CornerRadius(30f, 30f)
        return Outline.Rounded(RoundRect(0f, 295f, 900f /* width */, 1460f /* height */, cR, cR, cR, cR))
    }
}
