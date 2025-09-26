package com.example.ceygo.model

data class Review(
    val id: String = System.currentTimeMillis().toString(),
    val userId: String = "",
    val author: String,
    val userPhoto: String? = null,
    val rating: Int,            // 1..5
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isOwner: Boolean = false
)