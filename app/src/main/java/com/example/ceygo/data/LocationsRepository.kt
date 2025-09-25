package com.example.ceygo.data

import android.content.Context
import com.example.ceygo.data.firebase.FirestoreRepository
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

    fun initialize(context: Context) {
        if (initialized.compareAndSet(false, true)) {
            // Start collecting Firestore into memory state
            scope.launch {
                FirestoreRepository.observeAllLocations().collectLatest { list ->
                    _items.value = list
                }
            }
        }
    }

    fun location(id: String) = items.map { list -> list.firstOrNull { it.id == id } }

    fun locations(): List<Location> = _items.value

    fun addReview(locationId: String, review: Review) {
        scope.launch {
            FirestoreRepository.addReview(locationId, review.text, review.rating)
        }
    }
}