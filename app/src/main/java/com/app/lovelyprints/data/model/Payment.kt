package com.app.lovelyprints.data.model

data class CreatePaymentResponse(
    val id: String,
    val amount: Int
)



data class VerifyPaymentResponse(
    val success: Boolean,
    val message: String
)