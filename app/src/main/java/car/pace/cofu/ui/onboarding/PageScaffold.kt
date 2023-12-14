package car.pace.cofu.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.BuildConfig
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants.CONTAINER_CONTENT_TYPE
import car.pace.cofu.util.Constants.ONBOARDING_CONTENT_KEY
import car.pace.cofu.util.Constants.ONBOARDING_DESCRIPTION_KEY
import car.pace.cofu.util.Constants.ONBOARDING_IMAGE_KEY
import car.pace.cofu.util.Constants.ONBOARDING_TITLE_KEY
import car.pace.cofu.util.Constants.TITLE_CONTENT_TYPE

@Composable
fun PageScaffold(
    imageVector: ImageVector,
    @StringRes titleRes: Int,
    @StringRes nextButtonTextRes: Int,
    onNextButtonClick: () -> Unit,
    showCustomHeader: Boolean = BuildConfig.ONBOARDING_SHOW_CUSTOM_HEADER,
    nextButtonEnabled: Boolean = true,
    nextButtonLoading: Boolean = false,
    descriptionContent: @Composable ColumnScope.() -> Unit = {},
    errorText: String? = null,
    footerContent: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(
                key = ONBOARDING_IMAGE_KEY,
                contentType = CONTAINER_CONTENT_TYPE
            ) {
                Box(
                    modifier = Modifier.fillParentMaxHeight(0.53f),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (showCustomHeader) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_onboarding_header),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(bottom = 47.dp)
                                .fillMaxSize(),
                            alignment = Alignment.BottomCenter,
                            contentScale = ContentScale.Crop
                        )

                        Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                            modifier = Modifier
                                .size(68.dp)
                                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                                .padding(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = imageVector,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 20.dp, top = 12.dp, end = 20.dp)
                                .size(182.dp),
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
            item(
                key = ONBOARDING_TITLE_KEY,
                contentType = TITLE_CONTENT_TYPE
            ) {
                Title(
                    text = stringResource(id = titleRes),
                    modifier = Modifier.padding(start = 20.dp, top = 28.dp, end = 20.dp)
                )
            }
            item(
                key = ONBOARDING_DESCRIPTION_KEY,
                contentType = CONTAINER_CONTENT_TYPE
            ) {
                Column(
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp),
                    content = descriptionContent
                )
            }
            item(
                key = ONBOARDING_CONTENT_KEY,
                contentType = CONTAINER_CONTENT_TYPE
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    content = content
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            if (errorText != null) {
                Text(
                    text = errorText,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            footerContent()

            PrimaryButton(
                text = stringResource(id = nextButtonTextRes),
                modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 28.dp),
                enabled = nextButtonEnabled,
                loading = nextButtonLoading,
                onClick = onNextButtonClick
            )
        }
    }
}

@Preview
@Composable
fun PageScaffoldCustomHeaderPreview() {
    AppTheme {
        PageScaffold(
            imageVector = Icons.Outlined.TravelExplore,
            titleRes = R.string.onboarding_permission_title,
            nextButtonTextRes = R.string.onboarding_permission_action,
            onNextButtonClick = {},
            showCustomHeader = true,
            descriptionContent = {
                Description(
                    text = stringResource(id = R.string.onboarding_permission_description)
                )
            }
        )
    }
}

@Preview
@Composable
fun PageScaffoldDefaultPreview() {
    AppTheme {
        PageScaffold(
            imageVector = Icons.Outlined.TravelExplore,
            titleRes = R.string.onboarding_permission_title,
            nextButtonTextRes = R.string.onboarding_permission_action,
            onNextButtonClick = {},
            showCustomHeader = false,
            descriptionContent = {
                Description(
                    text = stringResource(id = R.string.onboarding_permission_description)
                )
            }
        )
    }
}
