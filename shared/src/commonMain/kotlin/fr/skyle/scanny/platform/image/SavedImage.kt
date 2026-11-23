package fr.skyle.scanny.platform.image

/**
 * An image kept in the photos, as the device finds it again: its content URI on Android, the local
 * identifier of its asset on iOS.
 */
data class SavedImage(val id: String)
