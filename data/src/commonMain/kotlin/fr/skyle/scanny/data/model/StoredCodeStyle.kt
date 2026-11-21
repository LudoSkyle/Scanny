package fr.skyle.scanny.data.model

/** How a created code is drawn, as stored. */
data class StoredCodeStyle(
    // ARGB
    val darkColor: Long,
    val lightColor: Long,
    // The names of CodeShape values of domain
    val dotShape: String,
    val cornerShape: String,
)
