package fr.skyle.scanny.ui.core.code.model

/** A code in a list: its type and what it holds, at a glance. */
data class CodeRowItem(
    val codeId: Long,
    val type: CodeType,
    val summary: String,
)