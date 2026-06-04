package xyz.northline.overmapper.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyz.northline.overmapper.data.db.dao.TrailDao
import xyz.northline.overmapper.data.db.dao.TrailPointDao
import xyz.northline.overmapper.data.db.entity.TrailEntity
import xyz.northline.overmapper.data.db.entity.TrailPointEntity
import xyz.northline.overmapper.domain.model.Trail
import xyz.northline.overmapper.domain.model.TrailPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrailRepository @Inject constructor(
    private val trailDao: TrailDao,
    private val trailPointDao: TrailPointDao
) {
    fun observeAll(): Flow<List<Trail>> = trailDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Trail? = trailDao.getById(id)?.toDomain()

    suspend fun getPointsForTrail(trailId: Long): List<TrailPoint> =
        trailPointDao.getByTrailId(trailId).map { it.toDomain() }

    fun observeAllPoints(): Flow<List<TrailPoint>> =
        trailPointDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun insertTrail(trail: TrailEntity): Long = trailDao.insert(trail)

    suspend fun updateTrail(trail: TrailEntity) = trailDao.update(trail)

    suspend fun insertPoints(points: List<TrailPointEntity>) = trailPointDao.insertAll(points)

    suspend fun insertPoint(point: TrailPointEntity): Long = trailPointDao.insert(point)

    suspend fun deleteTrail(id: Long) = trailDao.deleteById(id)
}

fun TrailEntity.toDomain() = Trail(id, recordedAt, distanceM, durationMs, elevationGainM,
    caloriesKcal, bboxSwLat, bboxSwLon, bboxNeLat, bboxNeLon, note)

fun TrailPointEntity.toDomain() = TrailPoint(id, trailId, segmentIndex, latitude, longitude,
    altitudeM, recordedAt)
