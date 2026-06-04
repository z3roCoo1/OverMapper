package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trail_point",
    foreignKeys = [ForeignKey(
        entity = TrailEntity::class,
        parentColumns = ["id"],
        childColumns = ["trail_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trail_id")]
)
data class TrailPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trail_id") val trailId: Long,
    @ColumnInfo(name = "segment_index") val segmentIndex: Int,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "altitude_m") val altitudeM: Double,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long
)
