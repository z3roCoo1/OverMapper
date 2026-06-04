package xyz.northline.overmapper.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import xyz.northline.overmapper.data.db.dao.*
import xyz.northline.overmapper.data.db.entity.*

@Database(
    entities = [TrailEntity::class, TrailPointEntity::class, MarkerEntity::class, TrailPhotoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OverMapperDatabase : RoomDatabase() {
    abstract fun trailDao(): TrailDao
    abstract fun trailPointDao(): TrailPointDao
    abstract fun markerDao(): MarkerDao
    abstract fun trailPhotoDao(): TrailPhotoDao
}
