package fr.skyle.scanny.domain.model

import kotlin.time.Instant

/** A code the user created: what it holds, the text put in it, and its format. */
data class Creation(
    val id: Long,
    val content: BarcodeContent,
    val rawValue: String,
    // Null for a format this version does not know, written by a later one
    val format: BarcodeFormat?,
    val createdAt: Instant,
)