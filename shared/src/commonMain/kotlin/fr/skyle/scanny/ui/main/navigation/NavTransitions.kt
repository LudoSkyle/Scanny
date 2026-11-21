package fr.skyle.scanny.ui.main.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

/*
 * The screen opened slides in from the end, over the screen it leaves, which slides a quarter of the
 * way and darkens; back, the other way round. Those of iOS, on both platforms: Android would cross-fade
 * the screens, and shrink the screen left by the back gesture.
 */

private const val TRANSITION_DURATION_MILLIS = 500

// Fast at first, slowing down long before the end, as iOS moves its screens
private val TransitionEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)

private fun <T> screenAnimationSpec(): FiniteAnimationSpec<T> =
    tween(
        durationMillis = TRANSITION_DURATION_MILLIS,
        easing = TransitionEasing,
    )

// Driven by the finger: the progress of the gesture is the progress of the transition
private fun <T> gestureAnimationSpec(): FiniteAnimationSpec<T> =
    tween(
        durationMillis = TRANSITION_DURATION_MILLIS,
        easing = LinearEasing,
    )

// The veils are still experimental in Compose
@OptIn(ExperimentalAnimationApi::class)
internal fun <T : Any> forwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = screenAnimationSpec(),
        ),
        initialContentExit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = screenAnimationSpec(),
            targetOffset = { offset -> offset / 4 },
        ) + veilOut(animationSpec = screenAnimationSpec()),
    )
}

@OptIn(ExperimentalAnimationApi::class)
internal fun <T : Any> backTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = screenAnimationSpec(),
            initialOffset = { offset -> offset / 4 },
        ) + unveilIn(animationSpec = screenAnimationSpec()),
        initialContentExit = slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = screenAnimationSpec(),
        ),
    )
}

// The screen left follows the finger, away from the edge the gesture starts from
@OptIn(ExperimentalAnimationApi::class)
internal fun <T : Any> backGestureTransition(): AnimatedContentTransitionScope<Scene<T>>.(swipeEdge: Int) -> ContentTransform = { swipeEdge ->
    val towards = if (swipeEdge == NavigationEvent.EDGE_RIGHT) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
    ContentTransform(
        targetContentEnter = slideIntoContainer(
            towards = towards,
            animationSpec = gestureAnimationSpec(),
            initialOffset = { offset -> offset / 4 },
        ) + unveilIn(animationSpec = gestureAnimationSpec()),
        initialContentExit = slideOutOfContainer(
            towards = towards,
            animationSpec = gestureAnimationSpec(),
        ),
    )
}