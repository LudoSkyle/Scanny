package fr.skyle.scanny.ui.core.code.model

import fr.skyle.scanny.domain.model.BarcodeFormat

/** A code in a list: its type, its format and what it holds, at a glance. */
data class CodeRowItem(
    val codeId: Long,
    val type: CodeType,
    // Null for one this version does not know
    val format: BarcodeFormat?,
    val summary: String,
)