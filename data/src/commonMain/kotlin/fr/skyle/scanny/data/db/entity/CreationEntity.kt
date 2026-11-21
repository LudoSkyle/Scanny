package fr.skyle.scanny.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A code the user created: its text, as put in the code, and its format. */
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
    // False when the user keeps no history of the creations: purged at the next start
    @ColumnInfo(name = "is_in_history", defaultValue = "1")
    val isInHistory: Boolean = true,
)