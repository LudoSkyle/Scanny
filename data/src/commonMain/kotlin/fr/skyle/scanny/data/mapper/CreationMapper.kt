package fr.skyle.scanny.data.mapper

import fr.skyle.scanny.data.db.entity.CreationEntity
import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.data.model.StoredCreation
import kotlin.time.Instant

internal fun CreationEntity.toStoredCreation(): StoredCreation =
    StoredCreation(
        id = id,
        rawValue = rawValue,
        format = format,
        style = StoredCodeStyle(
            darkColor = darkColor,
            lightColor = lightColor,
            cornerColor = cornerColor,
            gradient = gradient,
            gradientColor = gradientColor,
            dotShape = dotShape,
            cornerShape = cornerShape,
            logo = logo,
            logoShape = logoShape,
            logoMargin = logoMargin,
            errorCorrection = errorCorrection,
        ),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
    )