package cloud.pace.sdk.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

val Typography = Typography(
    h5 = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        lineHeight = 29.sp
    ),
    body1 = TextStyle(
        fontSize = 20.sp,
        lineHeight = 19.sp
    ),
    button = TextStyle(
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        lineHeight = 21.sp
    ),
    caption = TextStyle(
        fontSize = 16.sp,
        lineHeight = 19.sp
    ),
    overline = TextStyle(
        fontSize = 12.sp,
        lineHeight = 14.sp
    )
)
