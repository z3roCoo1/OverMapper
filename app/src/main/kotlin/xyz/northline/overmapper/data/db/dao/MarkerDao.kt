package xyz.northline.overmapper.data.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import xyz.northline.overmapper.data.db.entity.MarkerEntity

@Dao
interface MarkerDao {
    @Query("SELECT * FROM marker ORDER BY created_at DESC")
    fun observeAll(): Flow<List<MarkerEntity>>

    @Query("SELECT * FROM marker WHERE id = :id")
    suspend fun getById(id: Long): MarkerEntity?

    @Insert
    suspend fun insert(marker: MarkerEntity): Long

    @Update
    suspend fun update(marker: MarkerEntity)

    @Delete
    suspend fun delete(marker: MarkerEntity)
}
