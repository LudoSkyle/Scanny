package fr.skyle.scanny.platform.link

import fr.skyle.scanny.domain.model.BarcodeContent

/** The link that opens the messages app of the platform on [mms], written. */
internal expect fun platformMmsLink(mms: BarcodeContent.Mms): String