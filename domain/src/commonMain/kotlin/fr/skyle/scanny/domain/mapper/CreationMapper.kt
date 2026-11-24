package fr.skyle.scanny.domain.mapper

import fr.skyle.scanny.data.model.StoredCreation
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.payload.BarcodeContentParser

internal fun StoredCreation.toCreation(): Creation =
    Creation(
        id = id,
        content = BarcodeContentParser.parse(rawValue = rawValue),
        rawValue = rawValue,
        format = BarcodeFormat.entries.firstOrNull { it.name == format },
        style = style.toCodeStyle(),
        createdAt = createdAt,
        isFavorite = isFavorite,
    )