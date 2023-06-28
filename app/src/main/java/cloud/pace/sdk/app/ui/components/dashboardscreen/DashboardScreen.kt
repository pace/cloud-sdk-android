package cloud.pace.sdk.app.ui.components.dashboardscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cloud.pace.sdk.api.pay.generated.model.Transactions
import cloud.pace.sdk.app.R

@Composable
fun DashboardScreen(transactions: Transactions, openPaymentMethods: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ManagePaymentMethodsButton(onClick = openPaymentMethods)
        Text(
            text = "Recent transactions:",
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(0.dp, 4.dp),
            style = MaterialTheme.typography.body2
        )
        Divider(
            color = Color.Black,
            thickness = 2.dp,
            modifier = Modifier.padding(4.dp)
        )

        LazyColumn {
            items(transactions) {
                DashboardDataItemTemplate(it)
                Divider(
                    color = Color.Black,
                    thickness = 2.dp,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

@Composable
fun ManagePaymentMethodsButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .padding(6.dp)
            .height(50.dp)
            .width(300.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(text = "Manage payment methods")
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_settings_24),
            contentDescription = null,
            modifier = Modifier
                .size(46.dp)
                .padding(4.dp)
        )
    }
}
