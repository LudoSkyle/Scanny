package fr.skyle.scanny.data.model

import kotlin.time.Instant

/** A capture and its codes, in the order they were read. */
data class StoredScan(
    val id: Long,
    val scannedAt: Instant,
    val codes: List<StoredCode>,
    val isFavorite: Boolean,
)