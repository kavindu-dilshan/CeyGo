package com.example.ceygo.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ceygo.R
import com.example.ceygo.data.auth.AuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity(R.layout.activity_sign_up) {
    private lateinit var repo: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = AuthRepository.create(this)

        findViewById<MaterialButton>(R.id.btnSignUp).setOnClickListener { submit() }
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnGoSignIn).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun submit() {
        val tilName = findViewById<TextInputLayout>(R.id.tilName)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val name = findViewById<TextInputEditText>(R.id.etName).text?.toString()?.trim().orEmpty()
        val email = findViewById<TextInputEditText>(R.id.etEmail).text?.toString()?.trim().orEmpty()
        val password = findViewById<TextInputEditText>(R.id.etPassword).text?.toString().orEmpty()

        tilName.error = null; tilEmail.error = null; tilPassword.error = null

        var ok = true
        if (name.isBlank()) { tilName.error = "Required"; ok = false }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { tilEmail.error = "Invalid email"; ok = false }
        if (password.length < 8) { tilPassword.error = "Password must be 8 characters"; ok = false }
        if (!ok) return

        lifecycleScope.launch {
            val result = repo.signUp(name, email, password)
            result.onSuccess {
                // go to sign in
                startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
                finish()
            }.onFailure { e ->
                Snackbar.make(findViewById(android.R.id.content), e.message ?: "Sign up failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
