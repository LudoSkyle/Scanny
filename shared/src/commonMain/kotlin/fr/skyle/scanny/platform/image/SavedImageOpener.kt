package fr.skyle.scanny.platform.image

import androidx.compose.runtime.Composable

/**
 * Returns the action that shows a [SavedImage] in the photos of the device, which tells whether an app
 * could show it. Android opens the image itself in the gallery; iOS, which lets no app open a given
 * photo, opens the Photos app, where it is the newest.
 */
@Composable
internal expect fun rememberSavedImageOpener(): (SavedImage) -> Boolean
