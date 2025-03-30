package com.tejgokabhi.salonbooking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.ItemProductImagesBinding
import com.tejgokabhi.salonbooking.model.ProductImageUrlModel


class ProductImageAdapter(
    private val onClick: OnItemClickListener
):ListAdapter<ProductImageUrlModel, ProductImageAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemProductImagesBinding): RecyclerView.ViewHolder(binding.root)

    object DiffUtils: DiffUtil.ItemCallback<ProductImageUrlModel>() {
        override fun areItemsTheSame(oldItem: ProductImageUrlModel, newItem: ProductImageUrlModel): Boolean {
            return  oldItem.imageId == newItem.imageId
        }

        override fun areContentsTheSame(oldItem: ProductImageUrlModel, newItem: ProductImageUrlModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemProductImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            holder.binding.ivCarImage.load(item.imageUrl){
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

           holder.itemView.setOnClickListener {
                onClick.onItemClick(item)
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(imageUrlModel: ProductImageUrlModel)
    }
}