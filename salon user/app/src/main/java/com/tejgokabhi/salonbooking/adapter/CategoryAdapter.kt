package com.tejgokabhi.salonbooking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.ItemCategoryBinding
import com.tejgokabhi.salonbooking.model.CategoryModel


class CategoryAdapter(
    private val onClick: OnItemClickListener
):ListAdapter<CategoryModel, CategoryAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root)

    object DiffUtils: DiffUtil.ItemCallback<CategoryModel>() {
        override fun areItemsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return  oldItem.categoryId == newItem.categoryId
        }

        override fun areContentsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            ivCategoryImg.load(item.imgUrl){
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
            tvCategroyTitle.text = item.categoryName

           holder.itemView.setOnClickListener {
                onClick.onItemClick(item)
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(categoryModel: CategoryModel)
    }
}