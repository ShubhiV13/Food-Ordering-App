package com.example.recipeapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PaymentDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_details)

        val cashOnDeliveryButton = findViewById<Button>(R.id.cashOnDeliveryButton)
        val onlinePaymentButton = findViewById<Button>(R.id.onlinePaymentButton)
        val codForm = findViewById<LinearLayout>(R.id.codForm)
        val onlinePaymentForm = findViewById<LinearLayout>(R.id.onlinePaymentForm)
        val paymentDetailsText = findViewById<TextView>(R.id.paymentDetailsText)
        val finishButton = findViewById<Button>(R.id.finishButton)
        val deliveryAddress = findViewById<EditText>(R.id.deliveryAddress)
        val contactNumber = findViewById<EditText>(R.id.contactNumber)
        val cardNumber = findViewById<EditText>(R.id.cardNumber)
        val expiryDate = findViewById<EditText>(R.id.expiryDate)
        val cvv = findViewById<EditText>(R.id.cvv)
        val orderDetails = intent.getStringExtra("orderDetails") ?: ""
        val totalAmount = intent.getIntExtra("totalAmount", 0)
        val userName = intent.getStringExtra("userName") ?: "User"

        paymentDetailsText.apply {
            text = orderDetails
            setTextColor(Color.WHITE)
        }

        cashOnDeliveryButton.setOnClickListener {
            codForm.visibility = View.VISIBLE
            onlinePaymentForm.visibility = View.GONE
            paymentDetailsText.visibility = View.GONE
            Log.d("PaymentDetails", "Switched to Cash on Delivery form")
        }

        onlinePaymentButton.setOnClickListener {
            codForm.visibility = View.GONE
            onlinePaymentForm.visibility = View.VISIBLE
            paymentDetailsText.apply {
                visibility = View.VISIBLE
                text = orderDetails
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
            }
            Log.d("PaymentDetails", "Switched to Online Payment form")
        }

        finishButton.setOnClickListener {
            if (codForm.visibility == View.VISIBLE) {
                val address = deliveryAddress.text.toString().trim()
                val contact = contactNumber.text.toString().trim()

                if (validateAddress(address) && validateContact(contact)) {
                    Toast.makeText(this, "Order Confirmed with Cash on Delivery!", Toast.LENGTH_SHORT).show()
                    Log.d("PaymentDetails", "COD payment confirmed, Total: $totalAmount")
                    navigateToHome(totalAmount, userName)
                } else {
                    Toast.makeText(this, "Invalid address or contact number", Toast.LENGTH_SHORT).show()
                    Log.d("PaymentDetails", "COD validation failed")
                }
            } else if (onlinePaymentForm.visibility == View.VISIBLE) {
                val card = cardNumber.text.toString().trim()
                val expiry = expiryDate.text.toString().trim()
                val cvvCode = cvv.text.toString().trim()

                if (validateCardNumber(card) && validateExpiryDate(expiry) && validateCVV(cvvCode)) {
                    Toast.makeText(this, "Order Confirmed with Online Payment!", Toast.LENGTH_SHORT).show()
                    Log.d("PaymentDetails", "Online payment confirmed, Total: $totalAmount")
                    navigateToHome(totalAmount, userName)
                } else {
                    Toast.makeText(this, "Invalid card number, expiry date or CVV", Toast.LENGTH_SHORT).show()
                    Log.d("PaymentDetails", "Online payment validation failed")
                }
            }
        }
    }

    private fun validateAddress(address: String): Boolean {
        return address.isNotEmpty() && address.matches(Regex("^[a-zA-Z0-9\\s]+$"))
    }

    private fun validateContact(contact: String): Boolean {
        return contact.matches(Regex("^[0-9]{10}$"))
    }

    private fun validateCardNumber(card: String): Boolean {
        return card.matches(Regex("^[0-9]{16}$"))
    }

    private fun validateExpiryDate(expiry: String): Boolean {
        if (!expiry.matches(Regex("^[0-9]{2}/[0-9]{2}$"))) return false
        val parts = expiry.split("/")
        val month = parts[0].toIntOrNull() ?: return false
        val year = parts[1].toIntOrNull() ?: return false
        return month in 1..12 && year in 0..99
    }

    private fun validateCVV(cvv: String): Boolean {
        return cvv.matches(Regex("^[0-9]{3}$"))
    }

    private fun navigateToHome(totalAmount: Int, userName: String) {
        // Clear cart from SharedPreferences
        val sharedPreferences = getSharedPreferences("CartPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("cart") // This clears the entire cart; adjust if you want to set quantities to 0
        editor.apply()
        Log.d("PaymentDetails", "Cart cleared from SharedPreferences")

        val order = Order("Order ${System.currentTimeMillis()}", totalAmount, System.currentTimeMillis())
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("newOrder", "${order.id}:${order.total}:${order.timestamp}")
            putExtra("userName", userName)
            putExtra("clearCart", true)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish() // Close this activity
        Log.d("PaymentDetails", "Navigated to HomeActivity with clearCart: true")
    }
}
