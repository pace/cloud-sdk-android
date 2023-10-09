package car.pace.cofu.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultButton
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun PageScaffold(
    @DrawableRes imageRes: Int,
    @StringRes titleRes: Int,
    @StringRes descriptionRes: Int,
    @StringRes nextButtonTextRes: Int,
    onNextButtonClick: () -> Unit,
    nextButtonEnabled: Boolean = true,
    footerContent: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConstraintLayout(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 35.dp)
        ) {
            val (image, title, description, contentWrapper) = createRefs()
            val guideline = createGuidelineFromTop(0.10f)

            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.onboardingIconSize))
                    .constrainAs(image) {
                        top.linkTo(guideline)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
            Text(
                text = stringResource(id = titleRes),
                modifier = Modifier.constrainAs(title) {
                    top.linkTo(anchor = image.bottom, margin = 40.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(id = descriptionRes),
                modifier = Modifier.constrainAs(description) {
                    top.linkTo(anchor = title.bottom, margin = 14.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Column(
                modifier = Modifier.constrainAs(contentWrapper) {
                    top.linkTo(description.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(anchor = parent.bottom, margin = 20.dp)
                },
                content = content
            )
        }

        footerContent()

        DefaultButton(
            text = stringResource(id = nextButtonTextRes),
            modifier = Modifier.padding(horizontal = 35.dp),
            enabled = nextButtonEnabled,
            onClick = onNextButtonClick
        )
    }
}

@Preview
@Composable
fun PageScaffoldPreview() {
    AppTheme {
        PageScaffold(
            imageRes = R.drawable.ic_location,
            titleRes = R.string.ONBOARDING_PERMISSION_TITLE,
            descriptionRes = R.string.ONBOARDING_PERMISSION_DESCRIPTION,
            nextButtonTextRes = R.string.ONBOARDING_ACTIONS_SHARE_LOCATION,
            onNextButtonClick = {}
        )
    }
}
