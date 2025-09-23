package com.example.ceygo.ui

import androidx.lifecycle.ViewModel
import com.example.ceygo.data.LocationsRepository
import com.example.ceygo.model.Review

class LocationsViewModel : ViewModel() {
    fun location(id: String) = LocationsRepository.location(id)
    fun addReview(id: String, review: Review) = LocationsRepository.addReview(id, review)
}