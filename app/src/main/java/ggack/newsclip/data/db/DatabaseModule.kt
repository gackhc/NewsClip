package ggack.newsclip.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ggack.newsclip.data.models.Converters
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideClipDao(database : AppDataBase) : ClipDao {
        return database.clipDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext : Context, converter : Converters) : AppDataBase {
        return Room.databaseBuilder(
            appContext,
            AppDataBase::class.java,
            "clip_database"
        )
            .addTypeConverter(converter)
            .build()
    }
}