package fr.skyle.scanny.ui.screen.scandetail.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay

data class ScanDetailItem(
    val scanId: Long,
    val type: CodeType,
    val format: BarcodeFormat?,
    val scannedDay: CalendarDay,
    val scannedTime: String,
    val fields: List<CodeField>,
    val rawValue: String,
    val isRawValueShown: Boolean,
    val action: CodeAction?,
    // Of its capture: a group is a favorite as a whole, from its own screen
    val isFavorite: Boolean,
    val isInGroup: Boolean,
)