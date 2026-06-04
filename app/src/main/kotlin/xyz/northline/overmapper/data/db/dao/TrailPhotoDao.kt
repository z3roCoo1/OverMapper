package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.TrailPhotoEntity

@Dao
interface TrailPhotoDao {
    @Query("SELECT * FROM trail_photo WHERE trail_id = :trailId ORDER BY taken_at ASC")
    fun observeByTrailId(trailId: Long): Flow<List<TrailPhotoEntity>>

    @Insert
    suspend fun insert(photo: TrailPhotoEntity): Long

    @Delete
    suspend fun delete(photo: TrailPhotoEntity)
}
