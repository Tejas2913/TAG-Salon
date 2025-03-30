package com.tejgokabhi.salonbooking.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.adapter.BookingProductAdapter
import com.tejgokabhi.salonbooking.databinding.ActivityViewBookingsBinding
import com.tejgokabhi.salonbooking.model.BookingModel
import com.tejgokabhi.salonbooking.utils.Constants

class ViewBookingsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityViewBookingsBinding.inflate(layoutInflater) }
    private lateinit var database: FirebaseDatabase
    private lateinit var adminId: String  // Admin's unique Firebase UID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        adminId = Firebase.auth.currentUser!!.uid // Fetch current admin's UID

        binding.bookingRv.layoutManager = LinearLayoutManager(this)

        getAvailableBookings()

        binding.btBack.setOnClickListener {
            startActivity(Intent(this@ViewBookingsActivity, HomeMainActivity::class.java))
            finish()
        }
    }

    private fun getAvailableBookings() {
        val bookingList = ArrayList<BookingModel>()
        val userId = Firebase.auth.currentUser?.uid ?: return // Get logged-in user's ID

        val bookingsRef = database.getReference("BOOKING_REF") // Reference to all bookings

        bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingList.clear()

                for (adminSnapshot in snapshot.children) {  // Loop through all admins
                    val userBookings = adminSnapshot.child(userId) // Check if user has bookings under this admin

                    if (userBookings.exists()) {
                        for (bookingSnapshot in userBookings.children) { // Loop through user's bookings
                            val booking = bookingSnapshot.getValue(BookingModel::class.java)
                            booking?.let { bookingList.add(it) }
                        }
                    }
                }

                if (bookingList.isEmpty()) {
                    binding.tvStatus.visibility = View.VISIBLE
                    binding.tvStatus.text = "No Bookings Found"
                    binding.bookingRv.visibility = View.GONE
                } else {
                    binding.tvStatus.visibility = View.GONE
                    binding.bookingRv.visibility = View.VISIBLE
                    val bookingAdapter = BookingProductAdapter(this@ViewBookingsActivity)
                    bookingAdapter.submitList(bookingList)
                    binding.bookingRv.adapter = bookingAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.tvStatus.visibility = View.VISIBLE
                binding.tvStatus.text = "Error fetching bookings"
            }
        })
    }
}
