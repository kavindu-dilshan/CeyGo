package com.example.ceygo

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { view?.findViewById<ImageView>(R.id.imgAvatar)?.setImageURI(it) }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pre-fill demo data (could be loaded from your user store)
        view.findViewById<TextInputEditText>(R.id.etName).setText("Dileena Nethmini")
        view.findViewById<TextInputEditText>(R.id.etEmail).setText("dileenanethmini@gmail.com")
        view.findViewById<TextInputEditText>(R.id.etPassword).setText("password")

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        view.findViewById<View>(R.id.btnEditAvatar).setOnClickListener {
            pickImage.launch("image/*")
        }

        view.findViewById<MaterialButton>(R.id.btnUpdate).setOnClickListener {
            if (validateInputs(view)) {
                // TODO: persist to your backend / local store
                Snackbar.make(view, "Profile updated", Snackbar.LENGTH_SHORT).show()
            }
        }
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
        if (password.length < 6) {
            root.findViewById<TextInputLayout>(R.id.tilPassword).error = "Min 6 characters"
            ok = false
        }
        return ok
    }
}