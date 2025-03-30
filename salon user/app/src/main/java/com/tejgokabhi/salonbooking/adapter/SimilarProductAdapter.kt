package com.tejgokabhi.salonbooking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.ItemSimilerBinding
import com.tejgokabhi.salonbooking.model.ProductModel


class SimilarProductAdapter(
    private val favoriteList: ArrayList<ProductModel>,
    private val onClick: OnItemClickListener
):ListAdapter<ProductModel, SimilarProductAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemSimilerBinding): RecyclerView.ViewHolder(binding.root)

    object DiffUtils: DiffUtil.ItemCallback<ProductModel>() {
        override fun areItemsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return  oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: ProductModel, newItem: ProductModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemSimilerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {

            val matchFound = favoriteList.any { it.productId == item.productId }
            btFavorite.setImageResource(if (matchFound) R.drawable.ic_fav_filled_red else R.drawable.ic_fav_outline)

            ivProductImage.load(item.coverImg){
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }





            holder.binding.tvPrice.text = item.price


           holder.itemView.setOnClickListener {
                onClick.onItemClick(item)
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(productModel: ProductModel)
    }
}