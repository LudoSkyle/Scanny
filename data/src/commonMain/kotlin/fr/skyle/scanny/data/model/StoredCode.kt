package fr.skyle.scanny.data.model

import kotlin.time.Instant

data class StoredCode(
    val id: Long,
    val scanId: Long,
    val rawValue: String,
    val format: String,
    val scannedAt: Instant,
    // Whether its capture is a favorite, and how many codes it holds, this one included
    val isScanFavorite: Boolean,
    val scanCodeCount: Int,
)