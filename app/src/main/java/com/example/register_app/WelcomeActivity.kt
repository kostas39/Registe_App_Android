package com.example.register_app

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvSex: TextView
    private lateinit var tvAge: TextView
    private lateinit var btnLogout: Button
    private lateinit var ivPhoto: ImageView // Changed from ivProfilePhoto to ivPhoto

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Initialize UI components
        tvWelcome = findViewById(R.id.tvWelcome)
        tvSex = findViewById(R.id.tvSex)
        tvAge = findViewById(R.id.tvAge)
        btnLogout = findViewById(R.id.btnLogout)
        ivPhoto = findViewById(R.id.ivPhoto)

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Fetch user details from Firestore
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        tvWelcome.text = "Welcome, ${document.getString("username")}"
                        tvSex.text = document.getString("sex") ?: "N/A"
                        val yearOfBirth = document.getString("yearOfBirth")?.toIntOrNull() ?: 0
                        val currentYear = 2024
                        tvAge.text = (currentYear - yearOfBirth).toString()

                        // Decode Base64 string to Bitmap for profile photo
                        val photoString = document.getString("photo")
                        if (photoString != null) {
                            val imageBytes = android.util.Base64.decode(photoString, android.util.Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            ivPhoto.setImageBitmap(bitmap)
                        } else {
                            ivPhoto.setImageResource(R.drawable.ic_placeholder)
                        }
                    }
                }
        }

        // Logout functionality
        btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
