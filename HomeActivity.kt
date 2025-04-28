package com.example.recipeapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContent {
            RestaurantScreen(intent = intent)
        }
    }

    override fun onResume() {
        super.onResume()
        setContent {
            RestaurantScreen(intent = intent)
        }
    }
}

@Composable
fun RestaurantScreen(intent: Intent) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userName = remember { mutableStateOf(if (currentUser != null) intent.getStringExtra("userName") ?: "User" else "User") }
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var viewCount by remember { mutableStateOf(0) }

    val recipes = listOf(
        Recipe("Biryani", R.drawable.biryani, 150),
        Recipe("Pizza", R.drawable.pizza_sample, 200),
        Recipe("Chinese", R.drawable.chinese, 120),
        Recipe("Dosa", R.drawable.dosa, 20),
        Recipe("Pav Bhaji", R.drawable.pav_bhaji, 80),
        Recipe("Chole Bhature", R.drawable.chole_bhature, 100),
        Recipe("Idli", R.drawable.idli, 30),
        Recipe("Samosa", R.drawable.samosa, 40),
        Recipe("Vada Pav", R.drawable.vada_pav, 25),
        Recipe("Roti", R.drawable.roti, 15),
        Recipe("Dal Tadka", R.drawable.dal_tadka, 90),
        Recipe("Paneer Butter Masala", R.drawable.paneer_butter_masala, 180),
        Recipe("Noodles", R.drawable.noodles, 110),
        Recipe("Burger", R.drawable.burger, 150),
        Recipe("Paratha", R.drawable.paratha, 60),
        Recipe("Pulao", R.drawable.pulao, 130),
        Recipe("Masala Dosa", R.drawable.masala_dosa, 50),
        Recipe("Tandoori Chicken", R.drawable.tandoori_chicken, 250),
        Recipe("Raita", R.drawable.raita, 40),
        Recipe("Fried Rice", R.drawable.fried_rice, 140),
        Recipe("Sandwich", R.drawable.sandwich, 70),
        Recipe("Kebab", R.drawable.kebab, 200),
        Recipe("Upma", R.drawable.upma, 35),
        Recipe("Pasta", R.drawable.pasta, 160),
        Recipe("Aloo Paratha", R.drawable.aloo_paratha, 70),
        Recipe("Chicken Curry", R.drawable.chicken_curry, 220),
        Recipe("Rasgulla", R.drawable.rasgulla, 60),
        Recipe("Gulab Jamun", R.drawable.gulab_jamun, 80),
        Recipe("Fish Fry", R.drawable.fish_fry, 180),
        Recipe("Momos", R.drawable.momos, 90),
        Recipe("Shahi Paneer", R.drawable.shahi_paneer, 200),
        Recipe("Jalebi", R.drawable.jalebi, 50),
        Recipe("Thali", R.drawable.thali, 300),
        Recipe("Frankie", R.drawable.frankie, 100)
    )
    var filteredRecipes by remember { mutableStateOf(recipes) }
    var cart by remember { mutableStateOf<Map<Recipe, Int>>(emptyMap()) }
    var menuExpanded by remember { mutableStateOf(false) }
    var orders by remember { mutableStateOf(listOf<Order>()) }
    var showOrderHistory by remember { mutableStateOf(false) }

    val database = FirebaseDatabase.getInstance()
    val viewsRef: DatabaseReference = database.getReference("views")

    LaunchedEffect(Unit) {
        viewsRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val currentCount = currentData.getValue(Int::class.java) ?: 0
                currentData.value = currentCount + 1
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                viewCount = currentData?.getValue(Int::class.java) ?: 0
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Restaurants",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    menuExpanded = false
                    if (currentUser != null) {
                        Toast.makeText(context, "Name: ${userName.value}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please log in to view profile", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("My Profile")
                }
                DropdownMenuItem(onClick = {
                    menuExpanded = false
                    showOrderHistory = true
                }) {
                    Text("Order History")
                }
                DropdownMenuItem(onClick = {
                    menuExpanded = false
                    auth.signOut() // Sign out from Firebase
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val logoutIntent = Intent(context, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(logoutIntent)
                    (context as? ComponentActivity)?.finish() // Close HomeActivity
                }) {
                    Text("Log out")
                }
            }
        }

        if (showOrderHistory) {
            AlertDialog(
                onDismissRequest = { showOrderHistory = false },
                title = { Text("Order History") },
                text = {
                    Column {
                        orders.forEach { order ->
                            Text("Order ${order.id}: ₹${order.total} (Time: ${order.timestamp})")
                            Text("Payment: Completed")
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        if (orders.isEmpty()) {
                            Text("No orders yet.")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showOrderHistory = false }) {
                        Text("Close")
                    }
                }
            )
        }


        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                filteredRecipes = recipes.filter { recipe ->
                    recipe.name.contains(searchText.text, ignoreCase = true)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            },
            placeholder = { Text("Search Restaurants", color = Color.White) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = Color.White,
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                cursorColor = Color.White
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .weight(1f)
        ) {
            items(filteredRecipes.size) { index ->
                RecipeCard(recipe = filteredRecipes[index], cart = cart, onQuantityChange = { recipe, qty ->
                    val mutableCart = cart.toMutableMap()
                    if (qty > 0) {
                        mutableCart[recipe] = qty
                    } else {
                        mutableCart.remove(recipe)
                    }
                    cart = mutableCart.toMap()
                })
            }
        }

        Button(
            onClick = {
                if (cart.isNotEmpty()) {
                    val total = cart.entries.sumOf { it.key.price * it.value }
                    if (total > 0) {
                        val intent = Intent(context, ConfirmOrderActivity::class.java)
                        intent.putExtra("cart", cart.map { "${it.key.name}:${it.value}" }.toTypedArray())
                        intent.putExtra("userName", userName.value)
                        context.startActivity(intent)

                        orders = orders + Order("Order ${orders.size + 1}", total, System.currentTimeMillis())
                    } else {
                        Toast.makeText(context, "Total is zero! Please check quantities.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Cart is empty!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = cart.isNotEmpty()
        ) {
            Text("Confirm Order", color = Color.White)
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, cart: Map<Recipe, Int>, onQuantityChange: (Recipe, Int) -> Unit) {
    var quantity by remember { mutableStateOf(cart[recipe] ?: 0) }
    val maxQuantity = 10

    LaunchedEffect(quantity) {
        onQuantityChange(recipe, quantity)
    }

    Card(
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = recipe.imageRes),
                contentDescription = recipe.name,
                modifier = Modifier.size(100.dp)
            )
            Text("₹${recipe.price}", fontSize = 16.sp, color = Color.Green)
            Text(recipe.name, fontSize = 16.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                IconButton(onClick = { if (quantity > 0) quantity-- }) {
                    Text("-", fontSize = 20.sp)
                }
                Text("$quantity", fontSize = 16.sp, modifier = Modifier.width(24.dp))
                IconButton(onClick = { if (quantity < maxQuantity) quantity++ }) {
                    Text("+", fontSize = 20.sp)
                }
            }
        }
    }
}

data class Recipe(val name: String, val imageRes: Int, val price: Int)
data class Order(val id: String, val total: Int, val timestamp: Long)