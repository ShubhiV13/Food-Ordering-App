package com.example.recipeapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class ConfirmOrderActivity : AppCompatActivity() {

    private lateinit var startForResult: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_order)

        val itemContainer = findViewById<LinearLayout>(R.id.itemContainer)
        val totalAmountText = findViewById<TextView>(R.id.totalAmountText)
        val proceedToPaymentButton = findViewById<Button>(R.id.proceedToPaymentButton)
        val userName = intent.getStringExtra("userName") ?: "User"

        database = FirebaseDatabase.getInstance().reference

        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val paymentStatus = data?.getStringExtra("paymentStatus")
                    Log.d("ConfirmOrder", "Payment status received: $paymentStatus")

                    if (paymentStatus == "success") {
                        val totalAmount = intent.getIntExtra("totalAmount", 0)
                        val orderId = "Order_${System.currentTimeMillis()}"
                        val timestamp = System.currentTimeMillis()


                        val order = Order(orderId, totalAmount, timestamp)


                        database.child("orders").child(userName).child(orderId).setValue(order)
                            .addOnSuccessListener {
                                Log.d("Firebase", "Order saved successfully")


                                val sharedPreferences = getSharedPreferences("CartPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().remove("cart").apply()


                                val intent = Intent(this, HomeActivity::class.java).apply {
                                    putExtra("newOrder", "${order.id}:${order.total}:${order.timestamp}")
                                    putExtra("userName", userName)
                                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save order to Firebase", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Payment failed or cancelled", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        val cartItems = intent.getStringArrayExtra("cart")?.map { it.split(":") }?.map {
            Recipe(it[0], getImageResource(it[0]), getPrice(it[0]), it[1].toInt())
        } ?: emptyList()


        val sharedPreferences = getSharedPreferences("CartPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val cartString = cartItems.joinToString(separator = ",") { "${it.name}:${it.quantity}" }
        editor.putString("cart", cartString)
        editor.apply()

        var totalAmount = 0
        cartItems.forEach { recipe ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val nameText = TextView(this).apply {
                text = "Item: ${recipe.name}"
                textSize = 16f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 8, 8, 8) }
            }

            val quantityPriceText = TextView(this).apply {
                text = "Quantity: ${recipe.quantity}, Price: ₹${recipe.price * recipe.quantity}"
                textSize = 14f
                setTextColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(16, 0, 8, 8) }
            }

            itemLayout.addView(nameText)
            itemLayout.addView(quantityPriceText)
            itemContainer.addView(itemLayout)

            totalAmount += recipe.price * recipe.quantity
        }

        totalAmountText.apply {
            text = "Total Amount: ₹$totalAmount"
            setTextColor(Color.WHITE)
        }

        proceedToPaymentButton.setOnClickListener {
            val intent = Intent(this, PaymentDetailsActivity::class.java)
            val transactionId = UUID.randomUUID().toString().substring(0, 8)
            val orderDetails = "Order Details:\nUser: $userName\nTotal Amount: ₹$totalAmount\nTransaction ID: $transactionId"

            intent.putExtra("orderDetails", orderDetails)
            intent.putExtra("totalAmount", totalAmount)
            intent.putExtra("userName", userName)

            Log.d("ConfirmOrder", "Launching PaymentDetails with total: $totalAmount")
            startForResult.launch(intent)
        }
    }

    private fun getPrice(name: String): Int {
        return when (name) {
            "Biryani" -> 150
            "Dosa" -> 20
            "Pav Bhaji" -> 80
            "Chole Bhature" -> 100
            "Pizza" -> 200
            "Chinese" -> 120
            "Idli" -> 30
            "Samosa" -> 40
            "Vada Pav" -> 25
            "Roti" -> 15
            "Dal Tadka" -> 90
            "Paneer Butter Masala" -> 180
            "Noodles" -> 110
            "Burger" -> 150
            "Paratha" -> 60
            "Pulao" -> 130
            "Masala Dosa" -> 50
            "Tandoori Chicken" -> 250
            "Raita" -> 40
            "Fried Rice" -> 140
            "Sandwich" -> 70
            "Kebab" -> 200
            "Upma" -> 35
            "Pasta" -> 160
            "Aloo Paratha" -> 70
            "Chicken Curry" -> 220
            "Rasgulla" -> 60
            "Gulab Jamun" -> 80
            "Fish Fry" -> 180
            "Momos" -> 90
            "Shahi Paneer" -> 200
            "Jalebi" -> 50
            "Thali" -> 300
            "Frankie" -> 100
            else -> 0
        }
    }

    private fun getImageResource(name: String): Int {
        return when (name) {
            "Biryani" -> R.drawable.biryani
            "Dosa" -> R.drawable.dosa
            "Pav Bhaji" -> R.drawable.pav_bhaji
            "Chole Bhature" -> R.drawable.chole_bhature
            "Pizza" -> R.drawable.pizza_sample
            "Chinese" -> R.drawable.chinese
            "Idli" -> R.drawable.idli
            "Samosa" -> R.drawable.samosa
            "Vada Pav" -> R.drawable.vada_pav
            "Roti" -> R.drawable.roti
            "Dal Tadka" -> R.drawable.dal_tadka
            "Paneer Butter Masala" -> R.drawable.paneer_butter_masala
            "Noodles" -> R.drawable.noodles
            "Burger" -> R.drawable.burger
            "Paratha" -> R.drawable.paratha
            "Pulao" -> R.drawable.pulao
            "Masala Dosa" -> R.drawable.masala_dosa
            "Tandoori Chicken" -> R.drawable.tandoori_chicken
            "Raita" -> R.drawable.raita
            "Fried Rice" -> R.drawable.fried_rice
            "Sandwich" -> R.drawable.sandwich
            "Kebab" -> R.drawable.kebab
            "Upma" -> R.drawable.upma
            "Pasta" -> R.drawable.pasta
            "Aloo Paratha" -> R.drawable.aloo_paratha
            "Chicken Curry" -> R.drawable.chicken_curry
            "Rasgulla" -> R.drawable.rasgulla
            "Gulab Jamun" -> R.drawable.gulab_jamun
            "Fish Fry" -> R.drawable.fish_fry
            "Momos" -> R.drawable.momos
            "Shahi Paneer" -> R.drawable.shahi_paneer
            "Jalebi" -> R.drawable.jalebi
            "Thali" -> R.drawable.thali
            "Frankie" -> R.drawable.frankie
            else -> 0
        }
    }

    data class Recipe(val name: String, val imageRes: Int, val price: Int, val quantity: Int)
    data class Order(val id: String, val total: Int, val timestamp: Long)
}
