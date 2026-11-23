package fr.skyle.scanny.data.model

import kotlin.time.Instant

data class StoredCode(
    val id: Long,
    val scanId: Long,
    val rawValue: String,
    val format: String,
    val scannedAt: Instant,
)