package fr.skyle.scanny.ui.screen.history.ui.model

import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay

/** The captures of one day, from the newest to the oldest. */
data class ScanHistorySection(
    val day: CalendarDay,
    val items: List<ScanHistoryItem>,
)

/** A capture, shown by its first code. */
data class ScanHistoryItem(
    val scanId: Long,
    val firstCodeId: Long,
    val type: CodeType,
    val summary: String,
    val time: String,
    val codeCount: Int,
    // The types of its codes, each once, in their order: a group shows them
    val codeTypes: List<CodeType>,
)