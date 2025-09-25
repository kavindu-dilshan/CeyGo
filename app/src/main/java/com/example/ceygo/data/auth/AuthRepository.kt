package com.example.ceygo.data.auth

import android.content.Context
import android.util.Patterns
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(private val session: SessionManager) {

    companion object {
        fun create(context: Context): AuthRepository {
            return AuthRepository(SessionManager(context))
        }
    }

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun signUp(name: String, email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (name.isBlank()) return@withContext Result.failure(IllegalArgumentException("Name is required"))
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        if (password.length < 8) return@withContext Result.failure(IllegalArgumentException("Password must be at least 8 characters"))

        try {
            val cred = auth.createUserWithEmailAndPassword(email, password).await()
            val user = cred.user ?: return@withContext Result.failure(IllegalStateException("User create failed"))
            // Update profile display name
            val profileReq = com.google.firebase.auth.userProfileChangeRequest { displayName = name }
            user.updateProfile(profileReq).await()
            // Create/merge user doc
            firestore.collection("users").document(user.uid).set(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "avatarUri" to (user.photoUrl?.toString())
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String, keepLogged: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        return@withContext try {
            val cred = auth.signInWithEmailAndPassword(email, password).await()
            val user = cred.user ?: return@withContext Result.failure(IllegalStateException("Sign in failed"))
            if (keepLogged) session.rememberedUid = user.uid else session.clearRemembered()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(name: String, email: String, password: String?, avatarUri: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(IllegalStateException("Not signed in"))
        if (name.isBlank()) return@withContext Result.failure(IllegalArgumentException("Name is required"))
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        try {
            // Update profile
            val profileReq = com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
                avatarUri?.let { photoUri = android.net.Uri.parse(it) }
            }
            user.updateProfile(profileReq).await()
            if (user.email != email) user.updateEmail(email).await()
            if (!password.isNullOrBlank()) user.updatePassword(password).await()
            // Update Firestore doc
            firestore.collection("users").document(user.uid).set(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "avatarUri" to avatarUri
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCurrentUser() = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext
        try {
            firestore.collection("users").document(user.uid).delete().await()
            user.delete().await()
            session.clearRemembered()
        } catch (_: Exception) { }
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val res = auth.signInWithCredential(credential).await()
            val user = res.user ?: return@withContext Result.failure(IllegalStateException("Google sign-in failed"))
            session.rememberedUid = user.uid
            // Ensure user doc exists/updated
            firestore.collection("users").document(user.uid).set(
                mapOf(
                    "name" to (user.displayName ?: "User"),
                    "email" to (user.email ?: ""),
                    "avatarUri" to (user.photoUrl?.toString())
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun currentUid(): String? = auth.currentUser?.uid ?: session.rememberedUid
}
