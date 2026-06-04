package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trail_photo",
    foreignKeys = [ForeignKey(
        entity = TrailEntity::class,
        parentColumns = ["id"],
        childColumns = ["trail_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trail_id")]
)
data class TrailPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trail_id") val trailId: Long,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "file_uri") val fileUri: String,
    @ColumnInfo(name = "taken_at") val takenAt: Long
)
