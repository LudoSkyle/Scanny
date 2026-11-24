package fr.skyle.scanny.ui.screen.history.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.model.CodeType

/** The favorite creations, or the others of one day, from the newest to the oldest. */
data class CreationHistorySection(
    val header: HistoryHeader,
    val items: List<CreationHistoryItem>,
)

data class CreationHistoryItem(
    val creationId: Long,
    val type: CodeType,
    // Shown, the type already drawn: null for one this version does not know
    val format: BarcodeFormat?,
    val summary: String,
    val time: String,
    val isFavorite: Boolean,
)
