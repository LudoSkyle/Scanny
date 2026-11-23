package fr.skyle.scanny.ui.screen.scan.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeType

data class ScanResultItem(
    val codeId: Long,
    val type: CodeType,
    val format: BarcodeFormat?,
    val fields: List<CodeField>,
    val rawValue: String,
    val action: CodeAction?,
)