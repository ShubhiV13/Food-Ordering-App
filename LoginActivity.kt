package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val forgotPasswordText = findViewById<TextView>(R.id.forgotPasswordText)
        val signupText = findViewById<TextView>(R.id.signupText)

        loginButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                signInWithEmailAndPassword(name, email, password)
            }
        }

        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                false
            }
            password.length < 6 -> {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun signInWithEmailAndPassword(name: String, email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        fetchUserDetails(user.uid, name.ifEmpty { user.displayName ?: "User" })
                        saveUserData(user.uid, name.ifEmpty { user.displayName ?: "User" }, email)
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Unknown error"
                    Toast.makeText(this, "Login Failed: $errorMessage", Toast.LENGTH_LONG).show()
                    Log.e("Login", "Login failed: $errorMessage")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                Log.e("Login", "Failure: ${exception.message}")
            }
    }

    private fun fetchUserDetails(uid: String, defaultName: String) {
        val userRef = database.getReference("users").child(uid)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val storedName = snapshot.child("name").getValue(String::class.java) ?: defaultName
                val storedEmail = snapshot.child("email").getValue(String::class.java)
                val storedPhone = snapshot.child("phone").getValue(String::class.java)

                Log.d("User Details", "UID: $uid")
                Log.d("User Details", "Name: $storedName")
                Log.d("User Details", "Email: $storedEmail")
                Log.d("User Details", "Phone: $storedPhone")

                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.putExtra("userName", storedName)
                startActivity(intent)
                finish()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("User Details", "Error: ${error.message}")
                Toast.makeText(this@LoginActivity, "Error fetching details: ${error.message}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.putExtra("userName", defaultName)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun saveUserData(uid: String, name: String, email: String) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to ""
        )
        database.getReference("users").child(uid).setValue(userData)
            .addOnSuccessListener {
                Log.d("Login", "User data saved successfully for UID: $uid")
            }
            .addOnFailureListener { e ->
                Log.e("Login", "Error saving user data: ${e.message}")
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserDetails(currentUser.uid, currentUser.displayName ?: "User")
        }
    }
}