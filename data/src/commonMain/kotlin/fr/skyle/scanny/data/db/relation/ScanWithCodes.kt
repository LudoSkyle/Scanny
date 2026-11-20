package fr.skyle.scanny.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.db.entity.ScannedCodeEntity

// Room does not order the codes of a relation: the mapper sorts them by position
internal data class ScanWithCodes(
    @Embedded
    val scan: ScanEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "scan_id",
    )
    val codes: List<ScannedCodeEntity>,
)