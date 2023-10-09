package car.pace.cofu.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

class SnackbarData(
    @StringRes val messageRes: Int,
    vararg val messageFormatArgs: Any? = emptyArray(),
    @StringRes val actionLabelRes: Int? = null,
    val withDismissAction: Boolean = false,
    val duration: SnackbarDuration = if (actionLabelRes == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
    val onDismissed: () -> Unit = {},
    val onActionPerformed: () -> Unit = {}
)

suspend fun SnackbarData.showSnackbar(context: Context, snackbarHostState: SnackbarHostState) {
    val result = snackbarHostState.showSnackbar(
        message = context.getString(messageRes, *messageFormatArgs),
        actionLabel = actionLabelRes?.let { context.getString(it) },
        withDismissAction = withDismissAction,
        duration = duration
    )

    when (result) {
        SnackbarResult.ActionPerformed -> onActionPerformed()
        SnackbarResult.Dismissed -> onDismissed()
    }
}
