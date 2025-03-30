package com.tejgokabhi.salonbooking.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.UserLocationManager
import com.tejgokabhi.salonbooking.adapter.ProductAdapter
import com.tejgokabhi.salonbooking.databinding.ActivityCategoryBinding
import com.tejgokabhi.salonbooking.model.ProductModel
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils

class CategoryActivity : AppCompatActivity(), ProductAdapter.OnItemClickListener {
    private val binding by lazy { ActivityCategoryBinding.inflate(layoutInflater) }
    private lateinit var database: FirebaseDatabase
    private lateinit var productAdapter: ProductAdapter
    private lateinit var favoriteList: ArrayList<ProductModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        val category = intent.getStringExtra("categoryName")?.trim() ?: ""
        Log.d("CategoryIntent", "Received category: $category")
        binding.tvCategoryName.text = category

        binding.btBack.setOnClickListener {
            startActivity(Intent(this@CategoryActivity, HomeMainActivity::class.java))
            finish()
        }

        // Listen for changes in user location
        UserLocationManager.addListener { userLocation ->
            Log.d("CategoryActivit", "User Location (from Listener): $userLocation")
            getProducts(category, userLocation) // Now pass the updated location to getProducts
        }

        favoriteList = ArrayList()
        loadFavorites()
        productAdapter = ProductAdapter(favoriteList, this@CategoryActivity, this@CategoryActivity)

        // Initially try to fetch products, user location should already be set before this point.
        val userLocation = UserLocationManager.userLocation
        Log.d("CategoryActivit", "Initial User Location: $userLocation")  // Log initial user location

        if (userLocation != null) {
            getProducts(category, userLocation)  // Use the already set user location
        } else {
            Log.e("CategoryActivit", "User location is not set at this point.")
        }
    }

    private fun loadFavorites() {
        database.reference.child(Constants.FAVORITE_REF).child(Firebase.auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                favoriteList.clear()
                for (snapshot in dataSnapshot.children) {
                    val fav = snapshot.getValue(ProductModel::class.java)
                    if (fav != null) {
                        favoriteList.add(fav)
                    } else {
                        Log.e("Error", "Failed to convert data: ")
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Utils.showMessage(this@CategoryActivity, "Failed")
            }
        })
    }
    private fun getProducts(category: String, userLocation: String?) {
        val adminsRef = database.getReference("Admins") // Fetch from Admins to get Admin IDs
        val productsRef = database.getReference("Products") // Fetch products separately

        if (userLocation.isNullOrEmpty()) {
            Log.e("CategoryActivity", "User location is not set.")
            return
        }

        adminsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val productList = ArrayList<ProductModel>()

                for (adminSnapshot in dataSnapshot.children) {
                    val adminId = adminSnapshot.key // Admin ID
                    val salonName = adminSnapshot.child("salonName").getValue(String::class.java)?.trim()

                    if (salonName.isNullOrEmpty() || adminId.isNullOrEmpty()) {
                        Log.e("CategoryActivity", "Missing data for admin: ${adminSnapshot.key}")
                        continue
                    }

                    // Extracting location from salonName (could be modified depending on your DB structure)
                    val locationParts = salonName.split(",").map { it.trim() }
                    val locationPart = if (locationParts.size > 1) locationParts[2] else ""

                    if (locationPart.equals(userLocation.trim(), ignoreCase = true)) {
                        Log.d("CategoryActivity", "Location Matched: Fetching products for $salonName ($adminId)")

                        // Fetch products for the matched admin
                        productsRef.child(adminId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(productSnapshot: DataSnapshot) {
                                for (product in productSnapshot.children) {
                                    val productData = product.getValue(ProductModel::class.java)

                                    // Match category and ensure the productData is valid
                                    if (productData != null && productData.selectCategory.trim().equals(category.trim(), ignoreCase = true)) {
                                        productList.add(productData)
                                    }
                                }

                                Log.d("CategoryActivity", "Total Products Found: ${productList.size}")

                                // Hide status and show products if any are found
                                if (productList.isNotEmpty()) {
                                    binding.tvStatus.visibility = View.GONE
                                    binding.productsRV.visibility = View.VISIBLE
                                    productAdapter.submitList(productList)
                                    binding.productsRV.adapter = productAdapter
                                } else {
                                    binding.tvStatus.visibility = View.VISIBLE
                                    binding.productsRV.visibility = View.GONE
                                    binding.tvStatus.text = "No Products found"
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("Firebase", "Error fetching products: ${error.message}")
                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching data: ${databaseError.message}")
            }
        })
    }


    private fun manageFavorite(
        product: ProductModel,
        favList: ArrayList<ProductModel>,
        favBtn: ImageView
    ) {
        val databaseReference = database.reference.child(Constants.FAVORITE_REF).child(Firebase.auth.currentUser!!.uid).child(product.productId)

        val matchFound = favList.any { it.productId == product.productId }
        favBtn.setImageResource(if (matchFound) R.drawable.ic_fav_filled_red else R.drawable.ic_fav_outline)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    databaseReference.removeValue()
                        .addOnSuccessListener {
                            favBtn.setImageResource(R.drawable.ic_fav_outline)
                        }
                        .addOnFailureListener {
                            Utils.showMessage(this@CategoryActivity, "Something went wrong")
                        }
                } else {
                    databaseReference.setValue(product)
                        .addOnSuccessListener {
                            favBtn.setImageResource(R.drawable.ic_fav_filled_red)
                        }
                        .addOnFailureListener {
                            Utils.showMessage(this@CategoryActivity, "Something went wrong")
                        }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    override fun onItemClick(
        productModel: ProductModel,
        favBtn: ImageView,
        favList: ArrayList<ProductModel>
    ) {
        manageFavorite(productModel, favList, favBtn)
    }
}
