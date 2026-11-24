package fr.skyle.scanny.ui.screen.scan.ui.model

import fr.skyle.scanny.ui.core.code.model.CodeRowItem

/** What a scan found: one code, or the codes of a multiple scan. */
sealed interface ScanResult {

    data class Code(val item: ScanResultItem) : ScanResult

    data class Group(
        val scanId: Long,
        val codes: List<CodeRowItem>,
        val isFavorite: Boolean,
    ) : ScanResult
}

/** A multiple scan in progress: the number of codes found so far, each counted once. */
data class MultipleScanSession(
    val codeCount: Int,
    val isRecording: Boolean = false,
)