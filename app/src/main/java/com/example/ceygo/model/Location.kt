package com.example.ceygo.model

data class Location(
    val id: String,             // <- only this travels between screens
    val name: String,
    val province: String,
    val district: String,
    val description: String,
    val rating: Double,
    val images: List<String>,   // urls or @drawable names (see note below)
    val reviews: MutableList<Review> = mutableListOf()
) {
    val ratingAverage: Double
        get() = if (reviews.isEmpty()) rating else reviews.map { it.rating }.average()
}