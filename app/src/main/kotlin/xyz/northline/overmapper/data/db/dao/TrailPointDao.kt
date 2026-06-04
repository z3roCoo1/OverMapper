package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.TrailPointEntity

@Dao
interface TrailPointDao {
    @Query("SELECT * FROM trail_point WHERE trail_id = :trailId ORDER BY recorded_at ASC")
    suspend fun getByTrailId(trailId: Long): List<TrailPointEntity>

    @Query("SELECT * FROM trail_point ORDER BY trail_id ASC, segment_index ASC, recorded_at ASC")
    fun observeAll(): Flow<List<TrailPointEntity>>

    @Insert
    suspend fun insertAll(points: List<TrailPointEntity>)

    @Insert
    suspend fun insert(point: TrailPointEntity): Long

    @Query("DELETE FROM trail_point WHERE trail_id = :trailId")
    suspend fun deleteByTrailId(trailId: Long)
}
