package cloud.pace.sdk.app.ui.components.dashboardscreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cloud.pace.sdk.api.pay.generated.model.Transactions
import cloud.pace.sdk.app.R
import cloud.pace.sdk.app.ui.theme.Screen

@Composable
fun DashboardScreen(transactions: Transactions, openPaymentMethods: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = Screen.Dashboard.title,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            fontSize = 26.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(17.dp),
        )
        Divider(
            color = Color.Black,
            thickness = 3.dp,
        )
        ManagePaymentMethodsButton {
            openPaymentMethods()
        }
        Text(
            text = "recent transactions:",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary,
            fontSize = 16.sp,
            modifier = Modifier.padding(0.dp, 4.dp)
        )
        Divider(
            color = Color.Black,
            thickness = 2.dp,
            modifier = Modifier.padding(4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            item {
                LoadDashboardData(transactions)
            }
        }
    }
}

@Composable
fun LoadDashboardData(transactionList: Transactions) {
    transactionList.forEach {
        DashboardDataItemTemplate(it)
        Divider(
            color = Color.Black,
            thickness = 2.dp,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Composable
fun ManagePaymentMethodsButton(managePaymentMethodsButtonAction: () -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            Toast.makeText(context, "manage payment methods opened", Toast.LENGTH_SHORT).show()
            managePaymentMethodsButtonAction()
        },
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .padding(6.dp)
            .height(50.dp)
            .width(300.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(
            text = "Manage Payment Methods",
            fontSize = 16.sp
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_settings_24),
            "",
            modifier = Modifier
                .size(46.dp)
                .padding(4.dp)
        )
    }
}
