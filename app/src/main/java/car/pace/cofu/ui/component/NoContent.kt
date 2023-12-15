package car.pace.cofu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun LoadingCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    NoContentCard(
        modifier = modifier,
        header = {
            DefaultCircularProgressIndicator(
                modifier = Modifier.size(42.dp)
            )
        },
        title = title,
        description = description
    )
}

@Composable
fun ErrorCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.Outlined.ErrorOutline,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {}
) {
    NoContentCard(
        modifier = modifier,
        header = {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = title,
        description = description,
        buttonText = buttonText,
        onButtonClick = onButtonClick
    )
}

@Composable
fun NoContentCard(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    title: String,
    description: String,
    buttonText: String? = null,
    onButtonClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .dropShadow()
                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            header()

            Title(
                text = title,
                modifier = Modifier.padding(top = 20.dp)
            )
            Description(
                text = description,
                modifier = Modifier.padding(top = 20.dp)
            )

            if (buttonText != null) {
                PrimaryButton(
                    text = buttonText,
                    modifier = Modifier.padding(top = 20.dp),
                    onClick = onButtonClick
                )
            }
        }
    }
}

@Preview
@Composable
fun LoadingCardPreview() {
    AppTheme {
        LoadingCard(
            title = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_TITLE),
            description = stringResource(id = R.string.DASHBOARD_LOADING_VIEW_DESCRIPTION),
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview
@Composable
fun ErrorCardPreview() {
    AppTheme {
        ErrorCard(
            title = stringResource(id = R.string.general_error_title),
            description = stringResource(id = R.string.HOME_LOADING_FAILED_TEXT),
            modifier = Modifier.padding(20.dp)
        )
    }
}
