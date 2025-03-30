package com.tejgokabhi.salonbooking.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationCompat
import java.util.Calendar
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.tejgokabhi.salonbooking.R
import com.tejgokabhi.salonbooking.adapter.ButtonAdapter
import com.tejgokabhi.salonbooking.databinding.ActivityBookingBinding
import com.tejgokabhi.salonbooking.model.BookingModel
import com.tejgokabhi.salonbooking.model.ButtonModel
import com.tejgokabhi.salonbooking.preference.SharedPref
import com.tejgokabhi.salonbooking.service.TwilioNotificationService
import com.tejgokabhi.salonbooking.utils.Constants
import com.tejgokabhi.salonbooking.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookingActivity : AppCompatActivity(), ButtonAdapter.OnItemClickListener{
    private val binding by lazy { ActivityBookingBinding.inflate(layoutInflater) }
    private lateinit var database: FirebaseDatabase
    private var options = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()


        createSlots()

        binding.apply {

            val user = SharedPref.getUserData(this@BookingActivity)

            tvPrice.text = "₹ ${intent.getStringExtra(Constants.PRICE)}"
            etMessage.setText(intent.getStringExtra(Constants.SERVICE))
            etEmail.setText(user?.email)
            val name = "${user?.firstName} ${user?.lastName}"
            etName.setText(name)
            etPhoneNumber.setText(user?.phoneNumber)

            etDate.setOnClickListener {
                showDatePickerDialog()
            }

            btBack.setOnClickListener {
                startActivity(Intent(this@BookingActivity, HomeMainActivity::class.java))
                finish()
            }

            btBookNow.setOnClickListener {
                validateBooking()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format the selected date
                val formattedDate = "${selectedDay}/${selectedMonth + 1}/$selectedYear"
                binding.etDate.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }


    private fun createSlots() {
        binding.recyclerView.layoutManager = GridLayoutManager(this@BookingActivity, 3, GridLayoutManager.HORIZONTAL, false)
        val adapter = ButtonAdapter(this@BookingActivity)
        val buttonModelList = Constants.timeSlots.mapIndexed { index, time ->
            ButtonModel(id = "button_$index", btText = time)
        }
        adapter.submitList(buttonModelList)
        binding.recyclerView.adapter = adapter
    }

    private fun validateBooking() {
        binding.apply {
            val error = "Empty"
            if(etName.text.toString().isEmpty()) {
                etName.requestFocus()
                etName.error = error
            } else if(etEmail.text.toString().isEmpty()) {
                etEmail.requestFocus()
                etEmail.error = error
            } else if(etPhoneNumber.text.toString().isEmpty() || etPhoneNumber.text.toString().length != 13) {
                etPhoneNumber.requestFocus()
                etPhoneNumber.error = "Check"
            } else if(etMessage.text.toString().isEmpty()) {
                etMessage.requestFocus()
                etMessage.error = error
            } else if(options == "") {
                Utils.showMessage(this@BookingActivity, "Please Select a Slot")
            }else if(binding.etDate.text.toString().isEmpty()){
                binding.etDate.requestFocus()
                binding.etDate.error = "Select a date"
            }
            else {
                showBookingConfirmationDialog()
            }
        }
    }

    private fun showBookingConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmation, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Find the OK button from your custom layout
        val btConfirm = dialogView.findViewById<AppCompatButton>(R.id.btConfirm)

        btConfirm.setOnClickListener {
            dialog.dismiss()
            uploadBooking() // Save booking details to Firebase
        }

        dialog.show()
    }


    private fun uploadBooking() {
        val userId = Firebase.auth.currentUser!!.uid
        val productId = intent.getStringExtra(Constants.PRODUCTS_REF) ?: return
        val productsRef = database.getReference("Products")  // Changed from "Category" to "Products"

        productsRef.get().addOnSuccessListener { snapshot ->
            var foundAdminId: String? = null

            for (adminSnapshot in snapshot.children) {
                val adminId = adminSnapshot.key ?: continue  // Admin's unique ID

                for (productSnapshot in adminSnapshot.children) {
                    if (productSnapshot.key == productId) { // Check if this product belongs to the admin
                        foundAdminId = adminId
                        break
                    }
                }

                if (foundAdminId != null) break
            }

            if (foundAdminId != null) {
                saveBookingToAdmin(foundAdminId, userId)
            } else {
                Utils.showMessage(this@BookingActivity, "Admin not found for this service")
            }
        }.addOnFailureListener {
            Utils.showMessage(this@BookingActivity, "Failed to fetch admin data")
        }
    }


    private fun saveBookingToAdmin(adminId: String, userId: String) {
        val reference = database.getReference("BOOKING_REF").child(adminId).child(userId)
        val bookingId = reference.push().key!!

        val booking = BookingModel(
            bookingId,
            intent.getStringExtra(Constants.PRODUCTS_REF)!!,
            userId,
            binding.etName.text.toString(),
            binding.etEmail.text.toString(),
            binding.etPhoneNumber.text.toString(),
            binding.etMessage.text.toString(),
            options,
            binding.etDate.text.toString(),
            Utils.getCurrentTime(),
            "Pending"
        )

        reference.child(bookingId).setValue(booking)
            .addOnSuccessListener {
                resetBookingForm()
                Utils.showMessage(this@BookingActivity, "Booking Created Successfully")
                startActivity(Intent(this@BookingActivity, HomeMainActivity::class.java))
                showThankYouNotification(this)
                sendSmsToAdmin(adminId)
            }
            .addOnFailureListener {
                Utils.showMessage(this@BookingActivity, "Something went wrong")
            }
    }

    private fun sendSmsToAdmin(adminId: String) {
        // Fetch the admin phone number from Firebase and send SMS
        database.getReference("Admins").child(adminId).get()
            .addOnSuccessListener { snapshot ->
                try {
                    val adminPhone = snapshot.child("phoneNumber").value.toString()  // Ensure this path is correct
                    Log.d("Admin Phone: ", "Received $adminPhone")
                    // Launching a coroutine to send SMS
                    CoroutineScope(Dispatchers.IO).launch {
                        val message = "A new booking has been made. Check the admin panel for details."
                        TwilioNotificationService.sendSms(adminPhone, message)
                    }
                } catch (e: Exception) {
                    Log.e("Twilio", "Error sending SMS: ${e.message}")
                }
            }
            .addOnFailureListener {
                Log.e("Twilio", "Failed to fetch admin phone number: ${it.message}")
            }
    }

    private fun showThankYouNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "order_confirmation_channel"

        // Create a notification channel (required for API 26+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Order Confirmation", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent for when the user taps the notification
        val intent = Intent(context, HomeMainActivity::class.java) // Replace with your activity
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon) // Replace with your notification icon
            .setContentTitle("Booking Confirmed")
            .setContentText("Thank you for choosing us! We appreciate your business and hope you continue enjoying our app.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) // Dismiss the notification when tapped
            .build()

        // Show the notification
        notificationManager.notify(1001, notification) // Unique ID for the notification
    }


    private fun resetBookingForm() {
        binding.etName.text = null
        binding.etEmail.text = null
        binding.etPhoneNumber.text = null
        binding.etMessage.text = null
        binding.etDate.text = null
        binding.etMessage.clearFocus()
        options = ""
    }

    override fun onItemClick(buttonModel: ButtonModel) {
        options = buttonModel.btText
    }


}