package fr.skyle.scanny.domain.model

import kotlin.time.Instant

/** A code the user created: what it holds, the text put in it, its format, and how it is drawn. */
data class Creation(
    val id: Long,
    val content: BarcodeContent,
    val rawValue: String,
    // Null for a format this version does not know, written by a later one
    val format: BarcodeFormat?,
    val style: CodeStyle,
    val createdAt: Instant,
    // Kept in the history whatever its setting, never purged
    val isFavorite: Boolean,
)