package com.tejgokabhi.salonbooking.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.FirebaseDatabase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.activities.DetailsActivity
import com.tejgokabhi.salonbooking.databinding.ItemProductBinding
import com.tejgokabhi.salonbooking.model.ProductModel
import com.tejgokabhi.salonbooking.utils.Constants

class ProductAdapter(
    private val favoriteList: ArrayList<ProductModel>,
    private val onClick: OnItemClickListener,
    private val context: Context,
) : ListAdapter<ProductModel, ProductAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    object DiffUtils : DiffUtil.ItemCallback<ProductModel>() {
        override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {

            val matchFound = favoriteList.any { it.productId == item.productId }
            btFavorite.setImageResource(if (matchFound) R.drawable.ic_fav_filled_red else R.drawable.ic_fav_outline)

            val discount = item.price.toDouble() - item.fullPrice.toDouble()
            val discountPercentage = (discount/ item.price.toDouble()) * 100

            tvPercentage.text = "${discountPercentage.toInt()}% Off"

            tvTitle.text = item.title
            tvRatings.text = item.rating

            ivCarImg.load(item.coverImg) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }



            tvFullPrice.paintFlags = tvFullPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvFullPrice.text = "₹ ${item.price}"

            tvPrice.text = item.fullPrice
            fetchAdminId(item.productId, tvSalonName, item)

            holder.binding.tvSalonName.setOnClickListener {
                val fullAddress = item.salonName?.trim()

                // Debugging Log
                Log.d("MapsDebug", "Full Address Clicked: '$fullAddress'")

                if (fullAddress != null) {
                    if (fullAddress.isBlank()) {
                        Log.e("MapsDebug", "Error: fullAddress is empty or null!")
                    } else {
                        openGoogleMaps(holder.itemView.context, fullAddress)
                    }
                }
            }


            btFavorite.setOnClickListener {
                onClick.onItemClick(item, btFavorite, favoriteList)
            }

            btBook.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra(Constants.PRODUCTS_REF, item.productId)
                intent.putExtra("salonName", item.salonName)
                context.startActivity(intent)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra(Constants.PRODUCTS_REF, item.productId)
                intent.putExtra("salonName", item.salonName)
                context.startActivity(intent)
            }
        }

    }
    private fun fetchAdminId(productId: String, tvSalonName: TextView, item: ProductModel ) {
        val productsRef = FirebaseDatabase.getInstance().getReference("Products")

        productsRef.get().addOnSuccessListener { snapshot ->
            for (adminSnapshot in snapshot.children) {
                val adminId = adminSnapshot.key

                if (adminSnapshot.child(productId).exists()) {
                    Log.d("ProductAdapter", "Product $productId found under Admin ID: $adminId")
                    fetchSalonName(adminId!!, tvSalonName, item)
                    return@addOnSuccessListener
                }
            }
            Log.e("ProductAdapter", "Admin ID not found for Product ID: $productId")
            tvSalonName.text = "Unknown Salon"

        }.addOnFailureListener {
            Log.e("ProductAdapter", "Failed to fetch Admin ID: ${it.message}")
            tvSalonName.text = "Unknown Salon"
        }
    }

    private fun fetchSalonName(adminId: String, tvSalonName: TextView, item: ProductModel) {
        val adminRef = FirebaseDatabase.getInstance().getReference("Admins").child(adminId)

        adminRef.child("salonName").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val salonName = snapshot.value.toString()
                Log.d("ProductAdapter", "Fetched Salon Name: $salonName for Admin ID: $adminId")

                // ✅ Update the TextView
                tvSalonName.text = salonName

                // ✅ Store in ProductModel so that item.salonName is updated
                item.salonName = salonName

            } else {
                Log.e("ProductAdapter", "Salon Name not found for Admin ID: $adminId")
                tvSalonName.text = "Unknown Salon"
            }
        }.addOnFailureListener {
            Log.e("ProductAdapter", "Failed to fetch salon name: ${it.message}")
            tvSalonName.text = "Unknown Salon"
        }
    }
    private fun openGoogleMaps(context: Context, location: String) {
        if (location.isBlank()) {
            Toast.makeText(context, "Invalid location data!", Toast.LENGTH_SHORT).show()
            Log.e("MapsDebug", "Invalid location received: '$location'")
            return
        }

        // Trim spaces and format correctly
        val parts = location.split(",").map { it.trim() }
        val salonName = parts.getOrNull(0) ?: ""
        val areaName = parts.getOrNull(1) ?: ""
        val cityOrState = parts.getOrNull(2) ?: ""
        val pincode = parts.getOrNull(3) ?: ""

        // ✅ Build search query dynamically with pincode if available
        val searchQuery = when {
            pincode.isNotEmpty() -> "$salonName, $areaName, $cityOrState, $pincode"
            cityOrState.isNotEmpty() -> "$salonName, $areaName, $cityOrState"
            areaName.isNotEmpty() -> "$salonName, $areaName"
            else -> salonName
        }

        // Debugging Log
        Log.d("MapsDebug", "Final Search Query: '$searchQuery'")

        // Google Maps URL for search
        val mapsUri = Uri.parse("geo:0,0?q=${Uri.encode(searchQuery)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapsUri)
        mapIntent.setPackage("com.google.android.apps.maps") // Open in Google Maps app

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // If Google Maps app is missing, open in browser
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(searchQuery)}")
            )
            context.startActivity(webIntent)
        }
    }


    interface OnItemClickListener {
        fun onItemClick(
            productModel: ProductModel,
            favBtn: ImageView,
            favList: ArrayList<ProductModel>
        )
    }

}