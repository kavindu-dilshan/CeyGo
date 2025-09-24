package com.example.ceygo.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// Relations
data class LocationWithDetails(
    @Embedded val location: LocationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId"
    )
    val images: List<LocationImageEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId"
    )
    val reviews: List<ReviewEntity>
)

@Dao
interface LocationDao {
    @Transaction
    @Query("SELECT * FROM locations")
    fun getAllLocationsWithDetails(): Flow<List<LocationWithDetails>>

    @Transaction
    @Query("SELECT * FROM locations WHERE id = :id LIMIT 1")
    fun getLocationWithDetails(id: String): Flow<LocationWithDetails?>

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun countLocations(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocation(entity: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<LocationImageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("DELETE FROM location_images WHERE locationId = :locationId")
    suspend fun clearImagesForLocation(locationId: String)
}
