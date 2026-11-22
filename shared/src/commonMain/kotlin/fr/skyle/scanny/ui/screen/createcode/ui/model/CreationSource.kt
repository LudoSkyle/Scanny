package fr.skyle.scanny.ui.screen.createcode.ui.model

/** What the editor starts from: nothing, a scanned code or a created code, to be changed and created anew. */
sealed interface CreationSource {

    data object New : CreationSource

    data class ScannedCode(val codeId: Long) : CreationSource

    data class CreatedCode(val creationId: Long) : CreationSource
}
