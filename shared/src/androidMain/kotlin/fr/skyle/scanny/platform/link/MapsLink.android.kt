package fr.skyle.scanny.platform.link

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.payload.toGeoLink

// The maps apps of Android read the geo: link of RFC 5870
internal actual fun platformMapsLink(location: BarcodeContent.Location): String =
    location.toGeoLink()
