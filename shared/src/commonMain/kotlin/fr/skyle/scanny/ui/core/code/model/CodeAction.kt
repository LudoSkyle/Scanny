package fr.skyle.scanny.ui.core.code.model

import fr.skyle.scanny.domain.model.BarcodeContent

/** What the main button of a code does. */
sealed interface CodeAction {

    /** An action that opens another app on a link. */
    sealed interface Link : CodeAction {
        val link: String
    }

    data class OpenLink(override val link: String) : Link

    data class WriteEmail(override val link: String) : Link

    data class WriteSms(override val link: String) : Link

    data class Call(override val link: String) : Link

    data class ShowOnMap(override val link: String) : Link

    data class AddContact(val contact: BarcodeContent.Contact) : CodeAction

    data class JoinWifi(val wifi: BarcodeContent.Wifi) : CodeAction
}