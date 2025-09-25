package com.example.ceygo.data.auth

import android.content.Context
import android.util.Patterns
import com.example.ceygo.data.db.DatabaseProvider
import com.example.ceygo.data.db.user.UserDao
import com.example.ceygo.data.db.user.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthRepository(private val userDao: UserDao, private val session: SessionManager) {

    companion object {
        fun create(context: Context): AuthRepository {
            val db = DatabaseProvider.get(context)
            return AuthRepository(db.userDao(), SessionManager(context))
        }

        private fun hashPassword(password: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    suspend fun hasAnyUser(): Boolean = withContext(Dispatchers.IO) {
        userDao.countUsers() > 0
    }

    suspend fun signUp(name: String, email: String, password: String): Result<Int> = withContext(Dispatchers.IO) {
        if (name.isBlank()) return@withContext Result.failure(IllegalArgumentException("Name is required"))
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        if (password.length < 8) return@withContext Result.failure(IllegalArgumentException("Password must be at least 8 characters"))
        val existing = userDao.findByEmail(email)
        if (existing != null) return@withContext Result.failure(IllegalStateException("Email already exists"))
        val id = userDao.insert(UserEntity(name = name, email = email, passwordHash = hashPassword(password))).toInt()
        Result.success(id)
    }

    suspend fun signIn(email: String, password: String, keepLogged: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        val user = userDao.findByEmail(email) ?: return@withContext Result.failure(IllegalArgumentException("Email not found"))
        val hash = hashPassword(password)
        if (user.passwordHash != hash) return@withContext Result.failure(IllegalArgumentException("Incorrect password"))
        if (keepLogged) session.rememberedUserId = user.id else session.clearRemembered()
        Result.success(user.id)
    }

    suspend fun updateUser(id: Int, name: String, email: String, password: String?, avatarUri: String?): Result<Unit> = withContext(Dispatchers.IO) {
        val current = userDao.findById(id) ?: return@withContext Result.failure(IllegalArgumentException("User not found"))
        if (name.isBlank()) return@withContext Result.failure(IllegalArgumentException("Name is required"))
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        val updated = current.copy(
            name = name,
            email = email,
            passwordHash = password?.let { hashPassword(it) } ?: current.passwordHash,
            avatarUri = avatarUri
        )
        userDao.update(updated)
        Result.success(Unit)
    }

    suspend fun deleteUser(id: Int) = withContext(Dispatchers.IO) {
        userDao.findById(id)?.let { userDao.delete(it) }
        if (session.rememberedUserId == id) session.clearRemembered()
    }

    fun rememberedUserId(): Int = session.rememberedUserId
}
