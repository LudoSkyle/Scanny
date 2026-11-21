package fr.skyle.scanny.ui.screen.creationdetail.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay

data class CreationDetailItem(
    val type: CodeType,
    val format: BarcodeFormat?,
    val createdDay: CalendarDay,
    val createdTime: String,
    val fields: List<CodeField>,
    val rawValue: String,
    val style: CodeStyle,
)