package xyz.northline.overmapper.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trail")
data class TrailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
    @ColumnInfo(name = "distance_m") val distanceM: Float,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "elevation_gain_m") val elevationGainM: Float,
    @ColumnInfo(name = "calories_kcal") val caloriesKcal: Float?,
    @ColumnInfo(name = "bbox_sw_lat") val bboxSwLat: Double,
    @ColumnInfo(name = "bbox_sw_lon") val bboxSwLon: Double,
    @ColumnInfo(name = "bbox_ne_lat") val bboxNeLat: Double,
    @ColumnInfo(name = "bbox_ne_lon") val bboxNeLon: Double,
    val note: String? = null
)
