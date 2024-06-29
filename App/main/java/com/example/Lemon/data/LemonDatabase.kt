package com.example.arcus.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Singleton

import androidx.room.*



@Entity(tableName = "SavedWeatherLocations")
data class SavedWeatherLocationEntity(
    @PrimaryKey val nameOfLocation: String,
    val latitude: String,
    val longitude: String
)


@Database(entities = [SavedWeatherLocationEntity::class], version = 2)
abstract class ArcusDatabase : RoomDatabase() {
    abstract fun getDao(): ArcusDatabaseDao
}


@Dao
interface ArcusDatabaseDao {
    @Query("SELECT * FROM SavedWeatherLocations")
    fun getAllWeatherEntitiesMarkedAsNotDeleted(): Flow<List<SavedWeatherLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSavedWeatherEntity(weatherLocationEntity: SavedWeatherLocationEntity)
}

// Database Module for Dependency Injection remains largely unchanged
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideArcusDatabaseDao(
        @ApplicationContext context: Context
    ): ArcusDatabaseDao = Room.databaseBuilder(
        context = context,
        klass = ArcusDatabase::class.java,
        name = "Database"
    )
        // Ensure to add migration strategy here if necessary
        .fallbackToDestructiveMigration() // Use with caution, only if you're okay with losing data on schema change
        .build().getDao()
}

fun <T> Response<T>.getBodyOrThrowException(): T {

    val responseBody = body()
    if (responseBody == null) {

        val errorMessage = buildString {
            append("Failed to retrieve the response body.\n")
            append("HTTP Status Code: ${code()}\n")
            append("HTTP Status Message: ${message()}\n")
            append("URL: ${raw().request.url}\n")
            append("Headers: ${headers()}\n")
        }


        println("Error fetching response body: $errorMessage")


        throw IllegalArgumentException(errorMessage)
    }


    return responseBody
}
