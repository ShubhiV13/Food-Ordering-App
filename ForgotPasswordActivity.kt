package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import android.util.Log

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var errorTextView: TextView
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        Log.d("ForgotPassword", "Activity Started")

        auth = FirebaseAuth.getInstance()
        Log.d("ForgotPassword", "FirebaseAuth Initialized")

        emailEditText = findViewById(R.id.emailEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        errorTextView = findViewById(R.id.errorTextView)
        resetButton = findViewById(R.id.resetButton)
        Log.d("ForgotPassword", "Views Initialized")

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()
            Log.d("ForgotPassword", "Button Clicked - Email: $email, NewPassword: $newPassword")

            if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                errorTextView.text = "All fields are required"
                errorTextView.visibility = TextView.VISIBLE
                Log.w("ForgotPassword", "Validation Failed: Empty Fields")
                return@setOnClickListener
            }

            if (newPassword.length < 8) {
                errorTextView.text = "Password must be at least 8 characters"
                errorTextView.visibility = TextView.VISIBLE
                Log.w("ForgotPassword", "Validation Failed: Password too short")
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                errorTextView.text = "Passwords do not match"
                errorTextView.visibility = TextView.VISIBLE
                Log.w("ForgotPassword", "Validation Failed: Passwords mismatch")
                return@setOnClickListener
            }

            errorTextView.visibility = TextView.GONE
            resetPassword(email, newPassword)
        }
    }

    private fun resetPassword(email: String, newPassword: String) {
        Log.d("ForgotPassword", "Resetting Password for Email: $email")
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                val user = auth.currentUser
                Log.d("ForgotPassword", "Reset Email Sent, User: $user")
                user?.updatePassword(newPassword)
                    ?.addOnSuccessListener {
                        Log.d("ForgotPassword", "Password Updated Successfully")
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        // Explicitly navigate to LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    ?.addOnFailureListener { e ->
                        Log.e("ForgotPassword", "Password Update Failed: ${e.message}")
                        errorTextView.text = when (e) {
                            is FirebaseAuthInvalidUserException -> "No user found with this email"
                            else -> "Failed to update password: ${e.message}"
                        }
                        errorTextView.visibility = TextView.VISIBLE
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ForgotPassword", "Reset Email Failed: ${e.message}")
                errorTextView.text = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                    else -> "Reset email failed: ${e.message}"
                }
                errorTextView.visibility = TextView.VISIBLE
            }
    }
}