package fr.skyle.scanny.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A code the user created: its text, as put in the code, its format, and how it is drawn, its logo included. */
@Entity(
    tableName = "creation",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["is_in_history"]),
    ],
)
internal data class CreationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    // Epoch milliseconds
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "raw_value")
    val rawValue: String,
    // The name of a BarcodeFormat of domain
    @ColumnInfo(name = "format")
    val format: String,
    // ARGB, black by default: the creations of the version 3 were drawn dark on white
    @ColumnInfo(name = "dark_color", defaultValue = "4278190080")
    val darkColor: Long,
    // ARGB, white by default
    @ColumnInfo(name = "light_color", defaultValue = "4294967295")
    val lightColor: Long,
    // The names of CodeShape values of domain
    @ColumnInfo(name = "dot_shape", defaultValue = "SQUARE")
    val dotShape: String,
    @ColumnInfo(name = "corner_shape", defaultValue = "SQUARE")
    val cornerShape: String,
    // A PNG of a few hundred pixels a side at most, null for none
    @ColumnInfo(name = "logo", typeAffinity = ColumnInfo.BLOB)
    val logo: ByteArray?,
    // False when the user keeps no history of the creations: purged at the next start
    @ColumnInfo(name = "is_in_history", defaultValue = "1")
    val isInHistory: Boolean = true,
) {

    // The logo compared by its bytes, not by its array
    override fun equals(other: Any?): Boolean =
        other is CreationEntity &&
            id == other.id &&
            createdAt == other.createdAt &&
            rawValue == other.rawValue &&
            format == other.format &&
            darkColor == other.darkColor &&
            lightColor == other.lightColor &&
            dotShape == other.dotShape &&
            cornerShape == other.cornerShape &&
            logo.contentEquals(other.logo) &&
            isInHistory == other.isInHistory

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + rawValue.hashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + darkColor.hashCode()
        result = 31 * result + lightColor.hashCode()
        result = 31 * result + dotShape.hashCode()
        result = 31 * result + cornerShape.hashCode()
        result = 31 * result + logo.contentHashCode()
        result = 31 * result + isInHistory.hashCode()
        return result
    }
}