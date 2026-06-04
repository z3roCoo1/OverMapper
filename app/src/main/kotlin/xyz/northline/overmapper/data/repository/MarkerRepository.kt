package xyz.northline.overmapper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.northline.overmapper.data.db.dao.MarkerDao
import xyz.northline.overmapper.data.db.entity.MarkerEntity
import xyz.northline.overmapper.domain.model.Marker
import xyz.northline.overmapper.domain.model.MarkerType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarkerRepository @Inject constructor(private val dao: MarkerDao) {
    fun observeAll(): Flow<List<Marker>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun insert(marker: MarkerEntity): Long = dao.insert(marker)

    suspend fun update(marker: Marker) = dao.update(marker.toEntity())

    suspend fun delete(marker: Marker) = dao.delete(marker.toEntity())
}

fun MarkerEntity.toDomain() = Marker(id, trailId, latitude, longitude,
    MarkerType.valueOf(type), body, createdAt)

fun Marker.toEntity() = MarkerEntity(id, trailId, latitude, longitude,
    type.name, body, createdAt)
