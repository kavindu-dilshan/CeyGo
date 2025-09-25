package com.example.ceygo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ceygo.MainActivity
import com.example.ceygo.R
import com.example.ceygo.data.auth.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity(R.layout.activity_sign_in) {
    private lateinit var repo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = AuthRepository.create(this)

        findViewById<MaterialButton>(R.id.btnSignIn).setOnClickListener { submit() }
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.widget.TextView>(R.id.btnGoSignUp).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun submit() {
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val email = findViewById<TextInputEditText>(R.id.etEmail).text?.toString()?.trim().orEmpty()
        val password = findViewById<TextInputEditText>(R.id.etPassword).text?.toString().orEmpty()
        val keep = findViewById<CheckBox>(R.id.cbKeepLogged).isChecked

        tilEmail.error = null; tilPassword.error = null

        var ok = true
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.error = "Invalid email"; ok = false }
        if (password.isBlank()) { tilPassword.error = "Required"; ok = false }
        if (!ok) return

        lifecycleScope.launch {
            val result = repo.signIn(email, password, keep)
            result.onSuccess { id ->
                startActivity(Intent(this@SignInActivity, MainActivity::class.java).apply {
                    putExtra("user_id", id)
                })
                finish()
            }.onFailure { e ->
                Snackbar.make(findViewById(android.R.id.content), e.message ?: "Sign in failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
