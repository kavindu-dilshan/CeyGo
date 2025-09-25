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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity(R.layout.activity_sign_in) {
    private lateinit var repo: AuthRepository
    private lateinit var googleClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repo = AuthRepository.create(this)

        // Google Sign-In client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)

        findViewById<MaterialButton>(R.id.btnSignIn).setOnClickListener { submit() }
        findViewById<android.view.View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.widget.TextView>(R.id.btnGoSignUp).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.btnGoogle).setOnClickListener {
            startGoogleSignIn()
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
            result.onSuccess {
                startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                finish()
            }.onFailure { e ->
                Snackbar.make(findViewById(android.R.id.content), e.message ?: "Sign in failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun startGoogleSignIn() {
        val intent = googleClient.signInIntent
        startActivityForResult(intent, RC_GOOGLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                lifecycleScope.launch {
                    val res = repo.signInWithGoogle(account)
                    res.onSuccess {
                        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
                        finish()
                    }.onFailure { e ->
                        Snackbar.make(findViewById(android.R.id.content), e.message ?: "Google sign-in failed", Snackbar.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Snackbar.make(findViewById(android.R.id.content), e.message ?: "Google sign-in failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val RC_GOOGLE = 9001
    }
}
