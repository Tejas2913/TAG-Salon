package com.tejgokabhi.salonbooking.utils

import java.util.UUID

object Constants {

    //don't change this
    const val USER_REF = "Users"
    const val CATEGORY_REF = "Category"
    const val PRODUCTS_REF = "Products"
    const val FAVORITE_REF = "Favorite"
    const val NOTIFICATION_REF = "Notification"
    const val SLIDER_DOCUMENT = "Slider"
    const val BOOKING_REF = "Bookings"
    const val PRICE = "Price"
    const val SERVICE = "Service"
    val id = UUID.randomUUID().toString()
    val timeSlots = arrayListOf(
        "8:00 AM", "8:30 AM",
        "9:00 AM", "9:30 AM",
        "10:00 AM", "10:30 AM",
        "11:00 AM", "11:30 AM",
        "12:00 PM", "12:30 PM",
        "2:00 PM", "2:30 PM",
        "3:00 PM", "3:30 PM",
        "4:00 PM", "4:30 PM",
        "6:00 PM", "6:30 PM",
        "7:00 PM", "7:30 PM",
        "8:00 PM", "8:30 PM",
        "9:00 PM", "9:30 PM",
        "10:00 PM",

    )




}