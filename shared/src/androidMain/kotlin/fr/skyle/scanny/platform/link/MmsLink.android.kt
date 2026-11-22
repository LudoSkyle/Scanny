package fr.skyle.scanny.platform.link

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.payload.toMmsLink

// The messages apps of Android read the mms: link as they read the sms: one, and leave out its subject
internal actual fun platformMmsLink(mms: BarcodeContent.Mms): String =
    mms.toMmsLink()