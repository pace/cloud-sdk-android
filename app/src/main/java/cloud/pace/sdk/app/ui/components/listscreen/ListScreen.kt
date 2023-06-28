package cloud.pace.sdk.app.ui.components.listscreen

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cloud.pace.sdk.poikit.poi.GasStation

@Composable
fun ListScreen(gasStationList: List<GasStation>, location: Location?, permissionGranted: Boolean?) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        if (permissionGranted == false) {
            Snackbar {
                Text("Functions that require access to the fine location of this device do not work currently. You must first grant the permission if you want to use these functions!")
            }
        }
        LoadListData(gasStationList, location)
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
}
