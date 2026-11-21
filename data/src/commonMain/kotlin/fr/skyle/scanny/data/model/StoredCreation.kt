package fr.skyle.scanny.data.model

import kotlin.time.Instant

data class StoredCreation(
    val id: Long,
    val rawValue: String,
    // The name of a BarcodeFormat of domain
    val format: String,
    val createdAt: Instant,
)