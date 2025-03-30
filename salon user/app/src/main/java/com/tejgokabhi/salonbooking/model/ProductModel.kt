package com.tejgokabhi.salonbooking.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Parcelize
data class ProductModel (
    var productId: String = "",
    var coverImg: String = "",
    var title: String = "",
    var rating: String = "",
    var description: String = "",
    var price: String = "",
    var fullPrice: String = "",
    var selectCategory: String = "",
    var adminId: String = "",
    var salonName : String = "",
    @field:JvmField
    var images: @RawValue ArrayList<ProductImageUrlModel> = ArrayList()
): Parcelable
