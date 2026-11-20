package fr.skyle.scanny.ui.core.code.model

/** What the main button of a code does. */
sealed interface CodeAction {

    data class OpenLink(val link: String) : CodeAction

    data class WriteEmail(val link: String) : CodeAction

    data class WriteSms(val link: String) : CodeAction
}