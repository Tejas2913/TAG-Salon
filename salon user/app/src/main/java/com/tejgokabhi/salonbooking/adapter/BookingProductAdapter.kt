package com.tejgokabhi.salonbooking.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.activities.DetailsActivity
import com.tejgokabhi.salonbooking.databinding.ItemBookingsBinding
import com.tejgokabhi.salonbooking.model.BookingModel
import com.tejgokabhi.salonbooking.model.ProductModel
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils

class BookingProductAdapter(
    private val context: Context
) : ListAdapter<BookingModel, BookingProductAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemBookingsBinding) : RecyclerView.ViewHolder(binding.root)

    object DiffUtils : DiffUtil.ItemCallback<BookingModel>() {
        override fun areItemsTheSame(oldItem: BookingModel, newItem: BookingModel): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: BookingModel, newItem: BookingModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemBookingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            getProductData(item.productId, holder)
            tvName.text = item.name
            tvOption.text = item.timeSlot
            tvEmail.text = item.email
            tvMobile.text = item.phoneNumber
            tvBookingStatus.text = item.status
            tvDate.text = item.date

            val time = "This slot booked on ${item.timestamp}"
            tvTime.text = time

            holder.itemView.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra(Constants.PRODUCTS_REF, item.productId)
                context.startActivity(intent)
            }
        }

    }

    private fun getProductData(productId: String, holder: CategoryVH) {
        val productRef = Firebase.database.getReference(Constants.PRODUCTS_REF)

        productRef.get().addOnSuccessListener { snapshot ->
            var foundProduct: ProductModel? = null

            for (adminSnapshot in snapshot.children) { // Loop through admin nodes
                val productSnapshot = adminSnapshot.child(productId)
                if (productSnapshot.exists()) {
                    foundProduct = productSnapshot.getValue(ProductModel::class.java)
                    break
                }
            }

            if (foundProduct != null) {
                holder.binding.apply {
                    ivCarImg.load(foundProduct.coverImg) {
                        placeholder(R.drawable.placeholder)
                        error(R.drawable.placeholder)
                    }
                    val title = foundProduct.title
                    tvTitle.text = title
                    tvPrice.text = foundProduct.price
                    tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvOriginalPrice.text = "₹ ${foundProduct.price}"

                    val discount = foundProduct.price.toDouble() - foundProduct.fullPrice.toDouble()
                    val discountPercentage = (discount / foundProduct.price.toDouble()) * 100

                    tvPercentage.text = "${discountPercentage.toInt()}% Off"
                    tvPrice.text = foundProduct.fullPrice
                    tvPrice.visibility = View.VISIBLE
                    tvPercentage.visibility = View.VISIBLE
                    tvOriginalPrice.visibility = View.VISIBLE
                    tvTitle.visibility = View.VISIBLE
                }

            } else {
                Utils.showMessage(context, "Product not found")
            }
        }.addOnFailureListener {
            Utils.showMessage(context, "Failed to fetch product")
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