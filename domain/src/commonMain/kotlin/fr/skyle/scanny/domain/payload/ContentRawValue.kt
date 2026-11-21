package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent

/** The text to put in a code for this content, in the format the camera apps of Android and iOS read. */
fun BarcodeContent.toRawValue(): String =
    BarcodeContentEncoder.encode(content = this)