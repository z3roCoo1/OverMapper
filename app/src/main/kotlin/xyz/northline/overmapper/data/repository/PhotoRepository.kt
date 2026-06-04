package xyz.northline.overmapper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.northline.overmapper.data.db.dao.TrailPhotoDao
import xyz.northline.overmapper.data.db.entity.TrailPhotoEntity
import xyz.northline.overmapper.domain.model.TrailPhoto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(private val dao: TrailPhotoDao) {
    fun observeByTrailId(trailId: Long): Flow<List<TrailPhoto>> =
        dao.observeByTrailId(trailId).map { list -> list.map { it.toDomain() } }

    suspend fun insert(photo: TrailPhotoEntity): Long = dao.insert(photo)

    suspend fun delete(photo: TrailPhoto) = dao.delete(photo.toEntity())
}

fun TrailPhotoEntity.toDomain() = TrailPhoto(id, trailId, latitude, longitude, fileUri, takenAt)

fun TrailPhoto.toEntity() = TrailPhotoEntity(id, trailId, latitude, longitude, fileUri, takenAt)
