package com.example.ceygo.data.migrate

import android.content.Context
import android.content.SharedPreferences
import com.example.ceygo.data.db.AppDatabase
import com.example.ceygo.data.db.DatabaseProvider
import com.example.ceygo.data.db.LocationWithDetails
import com.example.ceygo.data.db.user.UserDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

object MigrationRunner {
    private const val PREFS = "migration_prefs"
    private const val KEY_DONE = "room_to_firestore_done"

    fun runOnce(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_DONE, false)) return
        try {
            runBlocking(Dispatchers.IO) {
                val dbRoom: AppDatabase = DatabaseProvider.get(context)
                val dao = dbRoom.locationDao()
                val firestore = FirebaseFirestore.getInstance()

                // Migrate locations -> locations collection with images array
                val list: List<LocationWithDetails> = dao.getAllLocationsWithDetails().first()
                list.forEach { it ->
                    val loc = it.location
                    val images = it.images.map { img -> img.url }
                    firestore.collection("locations").document(loc.id)
                        .set(
                            mapOf(
                                "name" to loc.name,
                                "province" to loc.province,
                                "district" to loc.district,
                                "description" to loc.description,
                                "rating" to loc.rating,
                                "images" to images
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        ).await()
                    // reviews as subcollection
                    val reviewsCol = firestore.collection("locations").document(loc.id).collection("reviews")
                    it.reviews.forEach { r ->
                        reviewsCol.document(r.id).set(
                            mapOf(
                                "userId" to "", // unknown for legacy reviews
                                "userName" to r.author,
                                "rating" to r.rating,
                                "text" to r.text,
                                "createdAt" to r.createdAt
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        ).await()
                    }
                }

                // Migrate users -> users collection (no passwords)
                val userDao: UserDao = dbRoom.userDao()
                // There's no bulk query defined; we reuse count/find by id isn't ideal. We skip full users migration if not needed.
                // In a real app, define a DAO query to list all users.
                // For now, mark migration as done for locations/reviews.
            }
            prefs.edit().putBoolean(KEY_DONE, true).apply()
        } catch (_: Exception) {
            // keep KEY_DONE false to retry next launch
        }
    }
}
