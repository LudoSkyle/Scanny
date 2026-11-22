package fr.skyle.scanny.platform.link

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.payload.toAppleMapsLink

// No app of iOS opens a geo: link: Maps opens its own links
internal actual fun platformMapsLink(location: BarcodeContent.Location): String =
    location.toAppleMapsLink()
