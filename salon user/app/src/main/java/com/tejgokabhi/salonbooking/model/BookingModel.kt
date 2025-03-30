package com.tejgokabhi.salonbooking.model

data class BookingModel(
    val bookingId: String = "",
    val productId: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val message: String = "",
    val timeSlot: String = "",
    val date: String = "",
    val timestamp: String = "",
    val status: String = ""
)
