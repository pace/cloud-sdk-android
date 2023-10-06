package car.pace.cofu.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

val Typography = Typography(
    titleMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        lineHeight = 29.sp
    ),
    titleSmall = TextStyle(
        color = Subtitle,
        fontSize = 16.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        lineHeight = 21.sp
    )
)
