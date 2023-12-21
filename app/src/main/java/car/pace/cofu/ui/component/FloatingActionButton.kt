package car.pace.cofu.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import car.pace.cofu.R
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.Constants.SEARCH_RESULT_CONTENT_TYPE
import car.pace.cofu.util.Constants.TRANSITION_DURATION
import com.google.android.libraries.places.api.model.AutocompletePrediction
import java.util.UUID

@Composable
fun SearchFloatingActionButton(
    query: TextFieldValue,
    results: List<AutocompletePrediction>,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    onQueryChange: (TextFieldValue) -> Unit,
    onResultClick: (AutocompletePrediction) -> Unit,
    onClick: () -> Unit
) {
    FloatingActionButtonSurface(
        expanded = expanded,
        modifier = modifier,
        collapsedSize = size
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingIconButton(
                    imageVector = Icons.Outlined.Search,
                    size = size,
                    onClick = onClick
                )

                if (expanded) {
                    SearchTextField(
                        value = query,
                        placeholder = stringResource(id = R.string.search_title),
                        onValueChange = onQueryChange
                    )
                }
            }

            if (expanded) {
                SearchResultsList(
                    searchResults = results,
                    onClick = onResultClick
                )
            }
        }
    }

    BackHandler(
        enabled = expanded,
        onBack = onClick
    )
}

@Composable
fun ExpandableFloatingActionButton(
    imageVector: ImageVector,
    text: String?,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val expanded = !text.isNullOrEmpty()

    FloatingActionButtonSurface(
        expanded = expanded,
        modifier = modifier,
        collapsedSize = size
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = expanded,
                modifier = Modifier.weight(weight = 1f, fill = false)
            ) {
                if (!text.isNullOrEmpty()) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(start = 14.dp, top = 9.dp, bottom = 9.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            FloatingIconButton(
                imageVector = imageVector,
                tint = tint,
                size = size,
                onClick = onClick
            )
        }
    }
}

@Composable
fun FloatingActionButtonSurface(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    collapsedSize: Dp = 50.dp,
    content: @Composable () -> Unit
) {
    val radius = if (expanded) 8.dp else collapsedSize / 2
    val cornerRadius by animateDpAsState(
        targetValue = radius,
        animationSpec = tween(TRANSITION_DURATION),
        label = "Corner radius transition"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 10.dp,
        content = content
    )
}

@Composable
fun FloatingIconButton(
    imageVector: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 50.dp,
    onClick: () -> Unit
) {
    Crossfade(
        targetState = imageVector to tint,
        label = "Image transition"
    ) { (icon, color) ->
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(contentColor = color),
            modifier = Modifier.size(size)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun SearchResultsList(
    searchResults: List<AutocompletePrediction>,
    onClick: (AutocompletePrediction) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        itemsIndexed(
            items = searchResults,
            key = { _, searchResult -> searchResult.placeId },
            contentType = { _, _ -> SEARCH_RESULT_CONTENT_TYPE }
        ) { index, searchResult ->
            Column(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = { onClick(searchResult) }
                )
            ) {
                if (index == 0) {
                    HorizontalDivider()
                }

                Text(
                    text = searchResult.getPrimaryText(null).toString(),
                    modifier = Modifier.padding(top = 14.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 17.sp,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = searchResult.getSecondaryText(null).toString(),
                    modifier = Modifier.padding(top = 2.dp, bottom = 14.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 18.sp
                )

                if (index != searchResults.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Preview
@Composable
fun SearchFloatingActionButtonPreview() {
    AppTheme {
        SearchFloatingActionButton(
            query = TextFieldValue(),
            results = emptyList(),
            expanded = false,
            modifier = Modifier.padding(20.dp),
            onQueryChange = {},
            onResultClick = {},
            onClick = {}
        )
    }
}

@Preview
@Composable
fun ExpandableFloatingActionButtonPreview() {
    AppTheme {
        ExpandableFloatingActionButton(
            imageVector = Icons.Outlined.Navigation,
            text = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE),
            modifier = Modifier.padding(20.dp),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FloatingIconButtonPreview() {
    AppTheme {
        FloatingIconButton(
            imageVector = Icons.Outlined.Search,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun SearchResultsListPreview() {
    AppTheme {
        val searchResults = remember {
            listOf(
                AutocompletePrediction.builder(UUID.randomUUID().toString()).setPrimaryText("Karlsruhe").setSecondaryText("Baden-Württemberg, Deutschland").build(),
                AutocompletePrediction.builder(UUID.randomUUID().toString()).setPrimaryText("Karlsruhe").setSecondaryText("76137, Baden-Württemberg, Deutschland").build(),
                AutocompletePrediction.builder(UUID.randomUUID().toString()).setPrimaryText("Knielingen").setSecondaryText("76187, Baden-Württemberg, Deutschland").build(),
                AutocompletePrediction.builder(UUID.randomUUID().toString()).setPrimaryText("Mühlburg").setSecondaryText("76185, Baden-Württemberg, Deutschland").build(),
                AutocompletePrediction.builder(UUID.randomUUID().toString()).setPrimaryText("Eggenstein-Leopoldshafen").setSecondaryText("76344, Baden-Württemberg, Deutschland").build()
            )
        }

        SearchResultsList(
            searchResults = searchResults,
            onClick = {}
        )
    }
}
