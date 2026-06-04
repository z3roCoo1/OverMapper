package xyz.northline.overmapper.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import xyz.northline.overmapper.data.db.OverMapperDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): OverMapperDatabase =
        Room.databaseBuilder(context, OverMapperDatabase::class.java, "overmapper.db").build()

    @Provides @Singleton fun provideTrailDao(db: OverMapperDatabase) = db.trailDao()
    @Provides @Singleton fun provideTrailPointDao(db: OverMapperDatabase) = db.trailPointDao()
    @Provides @Singleton fun provideMarkerDao(db: OverMapperDatabase) = db.markerDao()
    @Provides @Singleton fun provideTrailPhotoDao(db: OverMapperDatabase) = db.trailPhotoDao()
}
