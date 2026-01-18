package com.app.lovelyprints.data.model

import com.google.gson.annotations.SerializedName

data class CreatePaymentRequest(
    @SerializedName("order_id")
    val orderId: String
)
