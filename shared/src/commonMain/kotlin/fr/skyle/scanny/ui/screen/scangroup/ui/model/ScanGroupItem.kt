package fr.skyle.scanny.ui.screen.scangroup.ui.model

import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.date.model.CalendarDay

/** A capture of several codes, from a multiple scan. */
data class ScanGroupItem(
    val scannedDay: CalendarDay,
    val scannedTime: String,
    val codes: List<CodeRowItem>,
    // A favorite as a whole
    val isFavorite: Boolean,
)