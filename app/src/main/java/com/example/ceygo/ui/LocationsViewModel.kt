package com.example.ceygo.ui

import androidx.lifecycle.ViewModel
import com.example.ceygo.data.LocationsRepository
import com.example.ceygo.data.firebase.FirestoreRepository
import com.example.ceygo.model.Review
import kotlinx.coroutines.flow.Flow

class LocationsViewModel : ViewModel() {
    fun location(id: String) = LocationsRepository.location(id)
    fun addReview(id: String, review: Review) = LocationsRepository.addReview(id, review)
    fun reviews(id: String): Flow<List<Review>> = FirestoreRepository.observeReviews(id)
}