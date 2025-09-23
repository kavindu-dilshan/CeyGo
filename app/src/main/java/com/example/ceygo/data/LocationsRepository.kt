package com.example.ceygo.data

import com.example.ceygo.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

object LocationsRepository {
    private val _items = MutableStateFlow(seed())
    val items: StateFlow<List<Location>> = _items

    fun location(id: String) = items.map { list -> list.firstOrNull { it.id == id } }

    fun locations(): List<Location> = _items.value

    fun addReview(locationId: String, review: Review) {
        _items.update { list ->
            list.map { loc ->
                if (loc.id != locationId) loc
                else loc.copy(reviews = (loc.reviews + review).toMutableList())
            }
        }
    }
}

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