package fr.skyle.scanny.data.model

import kotlin.time.Instant

data class StoredScan(
    val id: Long,
    val rawValue: String,
    val format: String,
    val scannedAt: Instant,
)