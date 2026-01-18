package com.app.lovelyprints.data.model

import com.google.gson.annotations.SerializedName

data class CreatePaymentResponse(

    // Razorpay order id
    // Example: order_NpF2kLxGZb6xkM
    @SerializedName("id")
    val id: String,

    // Amount must be in paise
    // Example: 2500 = ₹25
    @SerializedName("amount")
    val amount: Int,

    // Optional
    @SerializedName("currency")
    val currency: String = "INR"
)
