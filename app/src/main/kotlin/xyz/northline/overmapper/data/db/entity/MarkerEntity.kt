package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "marker",
    foreignKeys = [ForeignKey(
        entity = TrailEntity::class,
        parentColumns = ["id"],
        childColumns = ["trail_id"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("trail_id")]
)
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "trail_id") val trailId: Long?,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val body: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
