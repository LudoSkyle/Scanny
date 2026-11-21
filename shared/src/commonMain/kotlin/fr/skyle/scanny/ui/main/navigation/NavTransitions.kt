package fr.skyle.scanny.ui.main.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.scene.Scene

/*
 * The screen opened slides in from the end and pushes the screen it leaves out, both across the whole
 * width; back, the other way round, and the back gesture plays the same transition as back.
 *
 * From and to the camera, the other screen fades in and out over it instead: the preview of a camera
 * moved or faded by Compose renders badly, so it stays still and opaque.
 */

/** Marks the screen that shows the camera, for its transitions to be fades. */
internal object CameraScreenKey : NavMetadataKey<Boolean>

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.involvesCameraScreen(): Boolean =
    initialState.metadata[CameraScreenKey] == true || targetState.metadata[CameraScreenKey] == true

internal fun <T : Any> forwardTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    if (involvesCameraScreen()) {
        ContentTransform(
            targetContentEnter = fadeIn(animationSpec = tween()),
            initialContentExit = ExitTransition.KeepUntilTransitionsFinished,
        )
    } else {
        ContentTransform(
            targetContentEnter = slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(),
            ),
            initialContentExit = slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(),
            ),
        )
    }
}

internal fun <T : Any> backTransition(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    if (involvesCameraScreen()) {
        ContentTransform(
            targetContentEnter = EnterTransition.None,
            initialContentExit = fadeOut(animationSpec = tween()),
        )
    } else {
        ContentTransform(
            targetContentEnter = slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(),
            ),
            initialContentExit = slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(),
            ),
        )
    }
}

// The edge the gesture starts from does not matter: the same transition as back
internal fun <T : Any> backGestureTransition(): AnimatedContentTransitionScope<Scene<T>>.(swipeEdge: Int) -> ContentTransform = {
    backTransition<T>().invoke(this)
}