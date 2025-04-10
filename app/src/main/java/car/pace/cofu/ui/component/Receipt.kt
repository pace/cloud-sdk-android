package car.pace.cofu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.component.shape.ReceiptShape
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.ShadowLight

@Composable
fun Receipt(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    padBorderPattern: Boolean = true,
    content: @Composable () -> Unit
) {
    val receiptShape = remember {
        ReceiptShape()
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shapeDropShadow(shape = receiptShape, color = ShadowLight, blur = 10.dp)
            .shapeDropShadow(shape = receiptShape, color = ShadowLight, blur = 4.dp)
            .background(color = backgroundColor, shape = receiptShape)
            .padding(vertical = if (padBorderPattern) 10.dp else 0.dp)
    ) {
        content()
    }
}

@Preview
@Composable
private fun ReceiptPreview() {
    AppTheme {
        Receipt(Modifier.padding(20.dp)) {
            Column {
                Text("Line 1")
                Text("Line 2")
            }
        }
    }
}
