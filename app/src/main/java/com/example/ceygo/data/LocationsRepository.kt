package com.example.ceygo.data

import android.content.Context
import androidx.room.Room
import com.example.ceygo.data.db.AppDatabase
import com.example.ceygo.data.db.LocationEntity
import com.example.ceygo.data.db.LocationImageEntity
import com.example.ceygo.data.db.LocationWithDetails
import com.example.ceygo.data.db.ReviewEntity
import com.example.ceygo.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

object LocationsRepository {
    // Backing state exposed to UI
    private val _items = MutableStateFlow<List<Location>>(emptyList())
    val items: StateFlow<List<Location>> = _items

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val initialized = AtomicBoolean(false)
    private lateinit var db: AppDatabase

    fun initialize(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            // Prefill in-memory state so screens have immediate data
            _items.value = seed()

            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ceygo.db"
            ).build()

            // Start collecting DB into memory state
            scope.launch {
                db.locationDao().getAllLocationsWithDetails().collectLatest { list ->
                    _items.update { list.map { it.toModel() } }
                }
            }

            // Seed on first run
            scope.launch { seedIfEmpty() }
        }
    }

    fun location(id: String) = items.map { list -> list.firstOrNull { it.id == id } }

    fun locations(): List<Location> = _items.value

    fun addReview(locationId: String, review: Review) {
        if (!this::db.isInitialized) return
        scope.launch {
            db.locationDao().insertReview(
                ReviewEntity(
                    id = review.id,
                    locationId = locationId,
                    author = review.author,
                    rating = review.rating,
                    text = review.text,
                    createdAt = review.createdAt
                )
            )
        }
    }

    private suspend fun seedIfEmpty() {
        val dao = db.locationDao()
        if (dao.countLocations() > 0) return

        // Insert all seed locations with images (and no initial reviews)
        for (loc in seed()) {
            dao.upsertLocation(
                LocationEntity(
                    id = loc.id,
                    name = loc.name,
                    province = loc.province,
                    district = loc.district,
                    description = loc.description,
                    rating = loc.rating
                )
            )
            dao.clearImagesForLocation(loc.id)
            dao.insertImages(loc.images.map { url ->
                LocationImageEntity(locationId = loc.id, url = url)
            })
            if (loc.reviews.isNotEmpty()) {
                dao.insertReviews(loc.reviews.map { r ->
                    ReviewEntity(
                        id = r.id,
                        locationId = loc.id,
                        author = r.author,
                        rating = r.rating,
                        text = r.text,
                        createdAt = r.createdAt
                    )
                })
            }
        }
    }
}

private fun LocationWithDetails.toModel(): Location = Location(
    id = location.id,
    name = location.name,
    province = location.province,
    district = location.district,
    description = location.description,
    rating = location.rating,
    images = images.map { it.url },
    reviews = reviews.map { r ->
        Review(
            id = r.id,
            author = r.author,
            rating = r.rating,
            text = r.text,
            createdAt = r.createdAt
        )
    }.toMutableList()
)

private fun seed(): List<Location> = listOf(
    Location(
        id = "sigiriya",
        name = "Sigiriya",
        province = "Central Province",
        district = "Matale",
        description = "Sigiriya or Sinhagiri is an ancient rock fortress...",
        rating = 4.5,
        images = listOf("https://picsum.photos/seed/sigirya/800/500")
    ),
    Location(
        id = "mirissa",
        name = "Mirissa Beach",
        province = "Southern Province",
        district = "Matara",
        description = "A beautiful beach in Sri Lanka.",
        rating = 4.8,
        images = listOf("https://picsum.photos/seed/mirissa/800/500")
    ),
    Location(
        id = "horton",
        name = "Horton Plains",
        province = "Central Province",
        district = "Nuwara Eliya",
        description = "A stunning national park with World's End viewpoint.",
        rating = 4.7,
        images = listOf("https://picsum.photos/seed/horton/800/500")
    ),
    Location(
        id = "ella",
        name = "Ella Rock",
        province = "Uva Province",
        district = "Badulla",
        description = "A famous hiking spot in Sri Lanka.",
        rating = 4.6,
        images = listOf("https://picsum.photos/seed/ella/800/500")
    )
)