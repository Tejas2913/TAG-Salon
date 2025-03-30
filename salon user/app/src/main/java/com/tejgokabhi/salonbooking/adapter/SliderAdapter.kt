package com.tejgokabhi.salonbooking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.ItemSliderBinding
import com.tejgokabhi.salonbooking.model.SliderModel


class SliderAdapter(private val onClick: OnItemClickListener): ListAdapter<SliderModel, SliderAdapter.CategoryVH>(DiffUtils){

    object DiffUtils: DiffUtil.ItemCallback<SliderModel>() {
        override fun areItemsTheSame(oldItem: SliderModel, newItem: SliderModel): Boolean {
            return oldItem.sliderId == newItem.sliderId
        }

        override fun areContentsTheSame(oldItem: SliderModel, newItem: SliderModel): Boolean {
            return oldItem == newItem
        }
    }

    inner class CategoryVH(val binding: ItemSliderBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemSliderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)

    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)
        holder.binding.apply {
            sliderImage.load(item.imageUrl){
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            holder.itemView.setOnClickListener {
                onClick.onItemClick(item)
            }


        }
    }

    interface OnItemClickListener {
        fun onItemClick(sliderModel: SliderModel)
    }

}



