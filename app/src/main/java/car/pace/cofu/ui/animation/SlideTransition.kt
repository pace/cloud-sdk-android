package car.pace.cofu.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import car.pace.cofu.util.Constants.TRANSITION_DURATION

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideIn(
    towards: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Start
): EnterTransition {
    return slideIntoContainer(
        towards = towards,
        animationSpec = tween(TRANSITION_DURATION)
    )
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.slideOut(
    towards: AnimatedContentTransitionScope.SlideDirection = AnimatedContentTransitionScope.SlideDirection.Start
): ExitTransition {
    return slideOutOfContainer(
        towards = towards,
        animationSpec = tween(TRANSITION_DURATION)
    )
}
