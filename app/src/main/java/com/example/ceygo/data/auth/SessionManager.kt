package com.example.ceygo.data.auth

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var rememberedUid: String?
        get() = prefs.getString(KEY_UID, null)
        set(value) = prefs.edit().putString(KEY_UID, value).apply()

    fun clearRemembered() {
        prefs.edit().remove(KEY_UID).apply()
    }

    companion object {
        private const val KEY_UID = "remembered_uid"
    }
}
