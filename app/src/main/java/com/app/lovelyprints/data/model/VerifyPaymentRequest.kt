package com.app.lovelyprints.data.model

import com.google.gson.annotations.SerializedName

data class VerifyPaymentRequest(

    @SerializedName("razorpay_order_id")
    val razorpayOrderId: String,

    @SerializedName("razorpay_payment_id")
    val razorpayPaymentId: String,

    @SerializedName("razorpay_signature")
    val razorpaySignature: String,

    @SerializedName("order_id")
    val orderId: String
)
