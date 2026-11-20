package fr.skyle.scanny.domain.model

import kotlin.time.Instant

data class Scan(
    val id: Long,
    val rawValue: String,
    val content: BarcodeContent,
    // Null for a format this version does not know, recorded by a newer one
    val format: BarcodeFormat?,
    val scannedAt: Instant,
)