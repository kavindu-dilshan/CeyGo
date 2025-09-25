package com.example.ceygo.data.firebase

import android.content.Context
import com.example.ceygo.model.Location
import com.example.ceygo.model.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirestoreRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Collections/paths
    private fun locationsCol() = db.collection("locations")
    private fun locationDoc(id: String) = locationsCol().document(id)
    private fun reviewsCol(locationId: String) = locationDoc(locationId).collection("reviews")

    private fun usersCol() = db.collection("users")
    private fun userDoc(uid: String) = usersCol().document(uid)
    private fun savedCol(uid: String) = userDoc(uid).collection("saved")

    fun observeAllLocations(): Flow<List<Location>> = callbackFlow {
        val reg = locationsCol().addSnapshotListener { snap, err ->
            if (err != null) { trySend(emptyList()) ; return@addSnapshotListener }
            if (snap == null) { trySend(emptyList()) ; return@addSnapshotListener }
            // We only store images as array on location doc for now
            val list = snap.documents.mapNotNull { d ->
                val id = d.id
                val name = d.getString("name") ?: return@mapNotNull null
                val province = d.getString("province") ?: ""
                val district = d.getString("district") ?: ""
                val description = d.getString("description") ?: ""
                val rating = d.getDouble("rating") ?: 0.0
                val images = (d.get("images") as? List<*>)?.map { it.toString() } ?: emptyList()
                Location(
                    id = id,
                    name = name,
                    province = province,
                    district = district,
                    description = description,
                    rating = rating,
                    images = images.toList(),
                    reviews = mutableListOf() // reviews stream separately in detail
                )
            }
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    fun observeLocation(locationId: String): Flow<Location?> = callbackFlow {
        val reg = locationDoc(locationId).addSnapshotListener { d, err ->
            if (err != null) { trySend(null); return@addSnapshotListener }
            if (d == null || !d.exists()) { trySend(null); return@addSnapshotListener }
            val name = d.getString("name") ?: return@addSnapshotListener
            val province = d.getString("province") ?: ""
            val district = d.getString("district") ?: ""
            val description = d.getString("description") ?: ""
            val rating = d.getDouble("rating") ?: 0.0
            val images = (d.get("images") as? List<*>)?.map { it.toString() } ?: emptyList()
            trySend(
                Location(
                    id = d.id,
                    name = name,
                    province = province,
                    district = district,
                    description = description,
                    rating = rating,
                    images = images.toList(),
                    reviews = mutableListOf()
                )
            )
        }
        awaitClose { reg.remove() }
    }

    fun observeReviews(locationId: String): Flow<List<Review>> = callbackFlow {
        val reg = reviewsCol(locationId)
            .orderBy("createdAt")
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { d ->
                    val author = d.getString("userName") ?: ""
                    val rating = (d.getLong("rating") ?: 0L).toInt()
                    val text = d.getString("text") ?: ""
                    val createdAt = d.getLong("createdAt") ?: 0L
                    Review(
                        id = d.id,
                        author = author,
                        rating = rating,
                        text = text,
                        createdAt = createdAt
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addReview(locationId: String, reviewText: String, rating: Int) {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val data = mapOf(
            "userId" to user.uid,
            "userName" to (user.displayName ?: user.email ?: "User"),
            "rating" to rating,
            "text" to reviewText,
            "createdAt" to System.currentTimeMillis()
        )
        reviewsCol(locationId).add(data).await()
    }

    suspend fun editReview(locationId: String, reviewId: String, newText: String, newRating: Int) {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val ref = reviewsCol(locationId).document(reviewId)
        val d = ref.get().await()
        if (d.getString("userId") != user.uid) throw IllegalAccessException("Cannot edit others' review")
        ref.update(mapOf("text" to newText, "rating" to newRating)).await()
    }

    suspend fun deleteReview(locationId: String, reviewId: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val ref = reviewsCol(locationId).document(reviewId)
        val d = ref.get().await()
        if (d.getString("userId") != user.uid) throw IllegalAccessException("Cannot delete others' review")
        ref.delete().await()
    }

    fun observeSaved(): Flow<Set<String>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptySet()); close(); return@callbackFlow }
        val reg = savedCol(uid).addSnapshotListener { snap, _ ->
            val ids = snap?.documents?.map { it.id }?.toSet() ?: emptySet()
            trySend(ids)
        }
        awaitClose { reg.remove() }
    }

    suspend fun toggleSaved(locationId: String) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        val doc = savedCol(uid).document(locationId)
        val get = doc.get().await()
        if (get.exists()) doc.delete().await() else doc.set(mapOf("savedAt" to FieldValue.serverTimestamp())).await()
    }

    // Popular (top 5 by average rating computed from reviews)
    fun observePopularTop5(): Flow<List<Location>> = callbackFlow {
        val reg = locationsCol().addSnapshotListener { snap, err ->
            if (err != null || snap == null) { trySend(emptyList()); return@addSnapshotListener }
            // We need to compute averages; fetch reviews for each (lightweight approach)
            val docs = snap.documents
            // Fetch reviews in parallel
            fun QuerySnapshot.toLocationsBase(): List<Location> = this.documents.mapNotNull { d ->
                val id = d.id
                val name = d.getString("name") ?: return@mapNotNull null
                val province = d.getString("province") ?: ""
                val district = d.getString("district") ?: ""
                val description = d.getString("description") ?: ""
                val rating = d.getDouble("rating") ?: 0.0
                val images = (d.get("images") as? List<*>)?.map { it.toString() } ?: emptyList()
                Location(id, name, province, district, description, rating, images, mutableListOf())
            }
            val baseList = snap.toLocationsBase()
            // Compute averages asynchronously via one-time fetch per location
            kotlinx.coroutines.GlobalScope.launch {
                try {
                    val withAvg = baseList.map { loc ->
                        val reviews = reviewsCol(loc.id).get().await().documents
                        val avg = if (reviews.isEmpty()) loc.rating else reviews.map { (it.getLong("rating") ?: 0L).toInt() }.average()
                        loc.copy(rating = avg)
                    }.sortedByDescending { it.rating }.take(5)
                    trySend(withAvg)
                } catch (_: Exception) {
                    trySend(emptyList())
                }
            }
        }
        awaitClose { reg.remove() }
    }

    fun observeNearbyRandom5(): Flow<List<Location>> = callbackFlow {
        val job = observeAllLocations()
            .onEach { list -> trySend(list.shuffled().take(5)) }
            .launchIn(CoroutineScope(Dispatchers.IO))
        awaitClose { job.cancel() }
    }
}
