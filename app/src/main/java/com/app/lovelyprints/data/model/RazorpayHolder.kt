package com.app.lovelyprints.data.model

data class RazorpayResult(
    val orderId: String,
    val paymentId: String,
    val signature: String
)

object RazorpayHolder {
    var result: RazorpayResult? = null
}
