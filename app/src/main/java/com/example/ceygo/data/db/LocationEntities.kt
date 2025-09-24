package com.example.ceygo.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val province: String,
    val district: String,
    val description: String,
    val rating: Double
)

@Entity(
    tableName = "location_images",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("locationId")]
)
data class LocationImageEntity(
    @PrimaryKey(autoGenerate = true) val imageId: Long = 0,
    val locationId: String,
    val url: String
)

@Entity(
    tableName = "reviews",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("locationId")]
)
data class ReviewEntity(
    @PrimaryKey val id: String,
    val locationId: String,
    val author: String,
    val rating: Int,
    val text: String,
    val createdAt: Long
)
