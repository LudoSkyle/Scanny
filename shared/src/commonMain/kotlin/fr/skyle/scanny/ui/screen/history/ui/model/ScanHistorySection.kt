package fr.skyle.scanny.ui.screen.history.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.model.CodeType

/** The favorite captures, or the others of one day, from the newest to the oldest. */
data class ScanHistorySection(
    val header: HistoryHeader,
    val items: List<ScanHistoryItem>,
)

/** A capture, shown by its first code. */
data class ScanHistoryItem(
    val scanId: Long,
    val firstCodeId: Long,
    val type: CodeType,
    // Shown for a single code, the type already drawn: null for one this version does not know
    val format: BarcodeFormat?,
    val summary: String,
    val time: String,
    val codeCount: Int,
    // The types of its codes, each once, in their order: a group shows them
    val codeTypes: List<CodeType>,
    val isFavorite: Boolean,
)