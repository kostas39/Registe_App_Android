package com.example.register_app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize UI components
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        // Add password visibility toggle
        setupPasswordVisibilityToggle(etPassword)

        // Check if the user is already logged in
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            navigateToWelcomeActivity() // Skip login and go directly to WelcomeActivity
        }

        // Login button click listener
        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Log in with Firebase Authentication
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            navigateToWelcomeActivity()
                        } else {
                            Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Register link click listener
        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Function to navigate to WelcomeActivity
    private fun navigateToWelcomeActivity() {
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity to prevent going back on back press
    }

    // Add the password visibility toggle
    private fun setupPasswordVisibilityToggle(editText: EditText) {
        val visibilityOn = resources.getDrawable(R.drawable.ic_visibility_on, null)

        var isPasswordVisible = false

        // Initially, no icon is displayed
        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        editText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                // Check if the touch is on the drawable end (right icon)
                val drawableEnd = 2 // Right drawable index
                val drawableWidth = editText.compoundDrawables[drawableEnd]?.bounds?.width() ?: 0 // Handle nullable width safely
                if (event.rawX >= (editText.right - drawableWidth)) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        // Show password and the visibility icon
                        editText.transformationMethod = null
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, visibilityOn, null)
                    } else {
                        // Hide password and remove the visibility icon
                        editText.transformationMethod = PasswordTransformationMethod.getInstance()
                        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                    editText.setSelection(editText.text.length) // Keep cursor at the end
                    return@setOnTouchListener true
                }
            } else if (event.action == MotionEvent.ACTION_DOWN && !isPasswordVisible) {
                // Add the visibility icon temporarily when user touches the field
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, visibilityOn, null)
            }
            false
        }
    }
}



