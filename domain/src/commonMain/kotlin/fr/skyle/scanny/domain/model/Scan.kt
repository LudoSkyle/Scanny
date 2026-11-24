package fr.skyle.scanny.domain.model

import kotlin.time.Instant

/** A capture: one code for a simple scan, all the codes found during a multiple scan. */
data class Scan(
    val id: Long,
    val scannedAt: Instant,
    val codes: List<ScannedCode>,
    // Kept in the history whatever its setting, never purged
    val isFavorite: Boolean,
)