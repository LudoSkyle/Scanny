package fr.skyle.scanny.ui.core.preview

import androidx.compose.ui.tooling.preview.Preview

private const val FRENCH_LOCALE = "fr"
private const val ENGLISH_LOCALE = "en"

/** Preview of a whole screen, or of what only shows over one (a dialog, a bottom sheet), in French and in English. */
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "FR",
    showSystemUi = true,
    locale = FRENCH_LOCALE,
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Preview(
    name = "EN",
    showSystemUi = true,
    locale = ENGLISH_LOCALE,
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
internal annotation class AppScreenPreview

/** Preview of a component, in French and in English. */
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "FR",
    locale = FRENCH_LOCALE,
)
@Preview(
    name = "EN",
    locale = ENGLISH_LOCALE,
)
internal annotation class AppPreview

/** Preview of a component drawn without a background of its own, in French and in English. */
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "FR",
    locale = FRENCH_LOCALE,
    showBackground = true,
)
@Preview(
    name = "EN",
    locale = ENGLISH_LOCALE,
    showBackground = true,
)
internal annotation class AppPreviewWithBackground