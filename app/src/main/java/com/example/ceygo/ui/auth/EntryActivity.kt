package com.example.ceygo.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ceygo.MainActivity
import com.example.ceygo.data.auth.AuthRepository
import kotlinx.coroutines.launch

class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = AuthRepository.create(this)
        val rememberedId = repo.rememberedUserId()
        if (rememberedId != -1) {
            // Go straight to app
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("user_id", rememberedId)
            })
            finish()
            return
        }

        lifecycleScope.launch {
            val hasUser = repo.hasAnyUser()
            if (!hasUser) {
                startActivity(Intent(this@EntryActivity, SignUpActivity::class.java))
            } else {
                startActivity(Intent(this@EntryActivity, SignInActivity::class.java))
            }
            finish()
        }
    }
}
