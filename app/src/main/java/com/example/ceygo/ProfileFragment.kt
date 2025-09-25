package com.example.ceygo

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.ceygo.data.auth.AuthRepository
import com.example.ceygo.data.db.DatabaseProvider
import android.content.Intent
import com.example.ceygo.ui.auth.SignInActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedAvatar = uri
            uri?.let { view?.findViewById<ImageView>(R.id.imgAvatar)?.load(it) }
        }

    private var selectedAvatar: Uri? = null
    private var currentUserId: Int = -1
    private lateinit var auth: AuthRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = AuthRepository.create(requireContext())

        // Determine current user id either from intent extra or remembered session
        val passedId = activity?.intent?.getIntExtra("user_id", -1) ?: -1
        currentUserId = if (passedId != -1) passedId else auth.rememberedUserId()

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        view.findViewById<View>(R.id.btnEditAvatar).setOnClickListener {
            pickImage.launch("image/*")
        }

        view.findViewById<MaterialButton>(R.id.btnUpdate).setOnClickListener {
            if (validateInputs(view)) {
                persistUpdate(view)
            }
        }

        view.findViewById<View>(R.id.btnDelete)?.setOnClickListener {
            deleteAccount()
        }

        // Load current user
        loadUser(view)
    }

    private fun validateInputs(root: View): Boolean {
        val name = root.findViewById<TextInputEditText>(R.id.etName).text?.toString()?.trim().orEmpty()
        val email = root.findViewById<TextInputEditText>(R.id.etEmail).text?.toString()?.trim().orEmpty()
        val password = root.findViewById<TextInputEditText>(R.id.etPassword).text?.toString().orEmpty()

        var ok = true
        root.findViewById<TextInputLayout>(R.id.tilName).error = null
        root.findViewById<TextInputLayout>(R.id.tilEmail).error = null
        root.findViewById<TextInputLayout>(R.id.tilPassword).error = null

        if (name.isEmpty()) {
            root.findViewById<TextInputLayout>(R.id.tilName).error = "Required"
            ok = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            root.findViewById<TextInputLayout>(R.id.tilEmail).error = "Invalid email"
            ok = false
        }
        if (password.isNotEmpty() && password.length < 8) {
            root.findViewById<TextInputLayout>(R.id.tilPassword).error = "Min 8 characters"
            ok = false
        }
        return ok
    }

    private fun loadUser(root: View) {
        if (currentUserId == -1) return
        viewLifecycleOwner.lifecycleScope.launch {
            // simple load via DAO through repo
            val db = DatabaseProvider.get(requireContext())
            val user = db.userDao().findById(currentUserId)
            user?.let {
                root.findViewById<TextInputEditText>(R.id.etName).setText(it.name)
                root.findViewById<TextInputEditText>(R.id.etEmail).setText(it.email)
                // do not set password for security; user can type new one
                it.avatarUri?.let { uri ->
                    root.findViewById<ImageView>(R.id.imgAvatar).load(uri)
                }
            }
        }
    }

    private fun persistUpdate(root: View) {
        val name = root.findViewById<TextInputEditText>(R.id.etName).text?.toString()?.trim().orEmpty()
        val email = root.findViewById<TextInputEditText>(R.id.etEmail).text?.toString()?.trim().orEmpty()
        val password = root.findViewById<TextInputEditText>(R.id.etPassword).text?.toString().orEmpty().ifBlank { null }
        val avatar = selectedAvatar?.toString()

        viewLifecycleOwner.lifecycleScope.launch {
            val res = auth.updateUser(currentUserId, name, email, password, avatar)
            res.onSuccess {
                Snackbar.make(root, "Profile updated", Snackbar.LENGTH_SHORT).show()
                // clear password field
                root.findViewById<TextInputEditText>(R.id.etPassword).setText("")
            }.onFailure { e ->
                Snackbar.make(root, e.message ?: "Update failed", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteAccount() {
        val root = view ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            auth.deleteUser(currentUserId)
            Snackbar.make(root, "Account deleted", Snackbar.LENGTH_SHORT).show()
            // Navigate to SignIn
            startActivity(Intent(requireContext(), SignInActivity::class.java))
            activity?.finish()
        }
    }
}