package com.tejgokabhi.salonbooking.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.UserLocationManager
import com.tejgokabhi.salonbooking.adapter.NotificationAdapter
import com.tejgokabhi.salonbooking.databinding.FragmentNotificationBinding
import com.tejgokabhi.salonbooking.model.NotificationModel
import com.tejgokabhi.salonbooking.utils.Constants


class NotificationFragment : Fragment() {
    private val binding by lazy { FragmentNotificationBinding.inflate(layoutInflater) }
    private lateinit var database: FirebaseDatabase
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance()
        notificationAdapter = NotificationAdapter(requireContext())

        binding.ivNotificationBack.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notification_to_navigation_home)
        }

        getAvailableNotifications()
    }

    private fun getAvailableNotifications() {
        val userArea = UserLocationManager.userLocation
        if (userArea.isNullOrEmpty()) {
            binding.tvNotificationStatus.visibility = View.VISIBLE
            binding.tvNotificationStatus.text = "Location not set"
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("Admins")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matchedAdminIds = mutableListOf<String>()

                for (adminSnapshot in snapshot.children) {
                    val salonName = adminSnapshot.child("salonName").getValue(String::class.java)
                    val adminId = adminSnapshot.key

                    if (!salonName.isNullOrEmpty() && adminId != null) {
                        if (salonName.contains(userArea, ignoreCase = true)) {
                            matchedAdminIds.add(adminId)
                        }
                    }
                }

                if (matchedAdminIds.isNotEmpty()) {
                    fetchNotificationsForAdmins(matchedAdminIds)
                } else {
                    binding.tvNotificationStatus.visibility = View.VISIBLE
                    binding.notificationRv.visibility = View.GONE
                    binding.tvNotificationStatus.text = "No Notifications"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.tvNotificationStatus.visibility = View.VISIBLE
                binding.tvNotificationStatus.text = "Error fetching notifications"
            }
        })
    }

    private fun fetchNotificationsForAdmins(adminIds: List<String>) {
        val notificationList = ArrayList<NotificationModel>()
        val databaseRef = FirebaseDatabase.getInstance().getReference(Constants.NOTIFICATION_REF)

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                notificationList.clear()

                for (adminId in adminIds) {
                    val adminSnapshot = dataSnapshot.child(adminId)
                    for (notificationSnapshot in adminSnapshot.children) {
                        val notification = notificationSnapshot.getValue(NotificationModel::class.java)
                        notification?.let {
                            notificationList.add(it)
                        }
                    }
                }

                if (notificationList.isNotEmpty()) {
                    binding.tvNotificationStatus.visibility = View.GONE
                    binding.notificationRv.visibility = View.VISIBLE
                    notificationAdapter.submitList(notificationList)
                    binding.notificationRv.adapter = notificationAdapter
                } else {
                    binding.tvNotificationStatus.visibility = View.VISIBLE
                    binding.notificationRv.visibility = View.GONE
                    binding.tvNotificationStatus.text = "No Notifications"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                binding.tvNotificationStatus.visibility = View.VISIBLE
                binding.tvNotificationStatus.text = "Error loading notifications"
            }
        })
    }
}
