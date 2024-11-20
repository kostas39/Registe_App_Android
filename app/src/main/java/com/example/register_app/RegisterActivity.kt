package com.example.register_app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRePassword: EditText
    private lateinit var etUsername: EditText
    private lateinit var spinnerYear: Spinner
    private lateinit var rgGender: RadioGroup
    private lateinit var btnUploadPhoto: Button
    private lateinit var btnRegister: Button
    private lateinit var btnClear: Button
    private lateinit var btnGoBack: Button
    private var encodedImage: String? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etRePassword = findViewById(R.id.etConfirmPassword)
        etUsername = findViewById(R.id.etUsername)
        spinnerYear = findViewById(R.id.spinnerYear)
        rgGender = findViewById(R.id.rgGender)
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto)
        btnRegister = findViewById(R.id.btnRegister)
        btnClear = findViewById(R.id.btnClear)
        btnGoBack = findViewById(R.id.btnGoBack)

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Populate spinner with years and a default prompt
        val currentYear = 2024
        val years = listOf("Please put your date of birth") + (1980..currentYear).map { it.toString() }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = adapter

        // Add password visibility toggle for etPassword
        setupPasswordVisibilityToggle(etPassword)

        // Add password visibility toggle for etRePassword
        setupPasswordVisibilityToggle(etRePassword)

        // Set up Upload Photo button
        btnUploadPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // Set up Register button
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val rePassword = etRePassword.text.toString()
            val username = etUsername.text.toString()
            val selectedYear = spinnerYear.selectedItem.toString()

            // Get selected gender
            val selectedGenderId = rgGender.checkedRadioButtonId
            val selectedGender = if (selectedGenderId != -1) {
                findViewById<RadioButton>(selectedGenderId).text.toString()
            } else {
                ""

            }

            if (email.isEmpty() || password.isEmpty() || rePassword.isEmpty() || username.isEmpty() || selectedYear == "Please put your date of birth" || selectedGender.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != rePassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (encodedImage == null) {
                Toast.makeText(this, "Please upload a photo", Toast.LENGTH_SHORT).show()
            } else {
                // Save user data to Firestore
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = firebaseAuth.currentUser?.uid
                            val userMap = mapOf(
                                "email" to email,
                                "username" to username,
                                "yearOfBirth" to selectedYear,
                                "sex" to selectedGender,
                                "photo" to encodedImage
                            )

                            userId?.let {
                                firestore.collection("users").document(it)
                                    .set(userMap)
                                    .addOnCompleteListener { dbTask ->
                                        if (dbTask.isSuccessful) {
                                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                            finish() // Go back to login
                                        } else {
                                            Toast.makeText(this, "Firestore error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Clear button functionality
        btnClear.setOnClickListener {
            etEmail.text.clear()
            etPassword.text.clear()
            etRePassword.text.clear()
            etUsername.text.clear()
            spinnerYear.setSelection(0) // Reset spinner to the prompt
            encodedImage = null
            rgGender.clearCheck() // Clear gender selection
        }

        // Go Back button functionality
        btnGoBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Closes the current activity
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                // Convert image to Base64 string
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Compress for size efficiency
                val imageBytes = outputStream.toByteArray()
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                Toast.makeText(this, "Photo selected successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to select photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to toggle password visibility
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
