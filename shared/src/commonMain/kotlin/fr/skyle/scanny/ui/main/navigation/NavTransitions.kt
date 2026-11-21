package fr.skyle.scanny.ui.main.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene
import androidx.navigationevent.NavigationEvent

/*
 * The screen opened slides in from the end, over the screen it leaves, which slides a quarter of the
 * way and darkens; back, the other way round. Those of iOS, on both platforms: Android would cross-fade
 * the screens, and shrink the screen left by the back gesture.
 *
 * From and to the camera, the other screen fades in and out over it instead: the preview of a camera
 * moved or faded by Compose renders badly, so it stays still and opaque.
 */

/** Marks the screen that shows the camera, for its transitions to be fades. */
internal object CameraScreenKey : NavMetadataKey<Boolean>

private const val TRANSITION_DURATION_MILLIS = 500
private const val FADE_DURATION_MILLIS = 300

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

private fun <T> fadeAnimationSpec(): FiniteAnimationSpec<T> =
    tween(durationMillis = FADE_DURATION_MILLIS)

private fun <T> gestureFadeAnimationSpec(): FiniteAnimationSpec<T> =
    tween(
        durationMillis = FADE_DURATION_MILLIS,
        easing = LinearEasing,
    )

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.involvesCameraScreen(): Boolean =
    initialState.metadata[CameraScreenKey] == true || targetState.metadata[CameraScreenKey] == true

// The veils are still experimental in Compose
@OptIn(ExperimentalAnimationApi::class)
internal fun <T : Any> forwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    if (involvesCameraScreen()) {
        ContentTransform(
            targetContentEnter = fadeIn(animationSpec = fadeAnimationSpec()),
            initialContentExit = ExitTransition.KeepUntilTransitionsFinished,
        )
    } else {
        slideForward()
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.slideForward(): ContentTransform =
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

internal fun <T : Any> backTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    if (involvesCameraScreen()) {
        ContentTransform(
            targetContentEnter = EnterTransition.None,
            initialContentExit = fadeOut(animationSpec = fadeAnimationSpec()),
        )
    } else {
        slideBack()
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.slideBack(): ContentTransform =
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

internal fun <T : Any> backGestureTransition(): AnimatedContentTransitionScope<Scene<T>>.(swipeEdge: Int) -> ContentTransform = { swipeEdge ->
    if (involvesCameraScreen()) {
        ContentTransform(
            targetContentEnter = EnterTransition.None,
            initialContentExit = fadeOut(animationSpec = gestureFadeAnimationSpec()),
        )
    } else {
        slideBackWithGesture(swipeEdge = swipeEdge)
    }
}

// The screen left follows the finger, away from the edge the gesture starts from
@OptIn(ExperimentalAnimationApi::class)
private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.slideBackWithGesture(swipeEdge: Int): ContentTransform {
    val towards = if (swipeEdge == NavigationEvent.EDGE_RIGHT) {
        AnimatedContentTransitionScope.SlideDirection.Left
    } else {
        AnimatedContentTransitionScope.SlideDirection.Right
    }
    return ContentTransform(
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