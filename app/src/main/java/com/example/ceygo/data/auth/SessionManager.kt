package com.example.ceygo.data.auth

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var rememberedUserId: Int
        get() = prefs.getInt(KEY_USER_ID, NO_USER)
        set(value) = prefs.edit().putInt(KEY_USER_ID, value).apply()

    fun clearRemembered() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }

    companion object {
        private const val KEY_USER_ID = "remembered_user_id"
        const val NO_USER = -1
    }
}
