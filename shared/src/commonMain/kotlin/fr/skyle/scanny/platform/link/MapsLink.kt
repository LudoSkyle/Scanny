package fr.skyle.scanny.platform.link

import fr.skyle.scanny.domain.model.BarcodeContent

/** The link that opens the maps app of the platform on [location], pinned. */
internal expect fun platformMapsLink(location: BarcodeContent.Location): String
