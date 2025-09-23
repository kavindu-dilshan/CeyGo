package com.example.ceygo.model

data class Review(
    val id: String = System.currentTimeMillis().toString(),
    val author: String,
    val rating: Int,            // 1..5
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)