package fr.skyle.scanny.domain.model

import kotlin.time.Instant

data class ScannedCode(
    val id: Long,
    val scanId: Long,
    val rawValue: String,
    val content: BarcodeContent,
    // Null for a format this version does not know, recorded by a newer one
    val format: BarcodeFormat?,
    val scannedAt: Instant,
    // Whether its capture is a favorite: a group is one as a whole
    val isFavorite: Boolean,
    // Whether its capture holds other codes
    val isInGroup: Boolean,
)