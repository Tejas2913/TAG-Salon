package com.tejgokabhi.salonbooking.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.databinding.ItemNotificationBinding
import com.tejgokabhi.salonbooking.model.NotificationModel

class NotificationAdapter(val context: Context):ListAdapter<NotificationModel, NotificationAdapter.CategoryVH>(DiffUtils) {
    inner class CategoryVH(val binding: ItemNotificationBinding): RecyclerView.ViewHolder(binding.root)

    object DiffUtils: DiffUtil.ItemCallback<NotificationModel>() {
        override fun areItemsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return  oldItem.notificationId == newItem.notificationId
        }

        override fun areContentsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVH {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryVH(binding)
    }

    override fun onBindViewHolder(holder: CategoryVH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            if(item.notificationImg.isNotEmpty()) {
                ivNotification.load(item.notificationImg){
                    placeholder(R.drawable.placeholder)
                    error(R.drawable.placeholder)
                }
            } else {
                ivNotification.visibility = View.GONE
            }
            tvNotificationHeader.text = item.notificationTitle
            tvNotificationDescription.text = item.notificationDescription
            holder.itemView.setOnClickListener {
                if(item.notificationUrl.isNotEmpty()) {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse(item.notificationUrl)
                    context.startActivity(openURL)
                }
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(notificationModel: NotificationModel)
    }
}