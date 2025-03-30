package com.tejgokabhi.salonbooking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.ItemButtonBinding
import com.tejgokabhi.salonbooking.model.ButtonModel


class ButtonAdapter(
    private val onClick: OnItemClickListener
) : ListAdapter<ButtonModel, ButtonAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemButtonBinding) : RecyclerView.ViewHolder(binding.root)

    private var selectedItemPosition = RecyclerView.NO_POSITION

    object DiffUtils : DiffUtil.ItemCallback<ButtonModel>() {
        override fun areItemsTheSame(oldItem: ButtonModel, newItem: ButtonModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ButtonModel, newItem: ButtonModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {

            button.text = item.btText

            if (position == selectedItemPosition) {
                button.setBackgroundResource(R.drawable.shape_solid)
                button.setTextColor(button.context.getColor(R.color.white))
            } else {
                button.setBackgroundResource(R.drawable.shape_outline)
                button.setTextColor(button.context.getColor(R.color.c10))
            }

            button.setOnClickListener {

                selectedItemPosition = holder.adapterPosition
                notifyDataSetChanged()
                onClick.onItemClick(item)
            }


        }

    }



    interface OnItemClickListener {
        fun onItemClick(buttonModel: ButtonModel)
    }
}