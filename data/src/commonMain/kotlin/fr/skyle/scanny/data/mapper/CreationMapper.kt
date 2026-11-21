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
            dotShape = dotShape,
            cornerShape = cornerShape,
        ),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
    )