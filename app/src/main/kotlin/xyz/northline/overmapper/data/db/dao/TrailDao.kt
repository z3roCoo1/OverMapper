package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.TrailEntity

@Dao
interface TrailDao {
    @Query("SELECT * FROM trail ORDER BY recorded_at DESC")
    fun observeAll(): Flow<List<TrailEntity>>

    @Query("SELECT * FROM trail WHERE id = :id")
    suspend fun getById(id: Long): TrailEntity?

    @Insert
    suspend fun insert(trail: TrailEntity): Long

    @Update
    suspend fun update(trail: TrailEntity)

    @Delete
    suspend fun delete(trail: TrailEntity)

    @Query("DELETE FROM trail WHERE id = :id")
    suspend fun deleteById(id: Long)
}
