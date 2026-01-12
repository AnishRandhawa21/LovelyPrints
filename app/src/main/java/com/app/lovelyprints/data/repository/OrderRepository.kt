package com.app.lovelyprints.data.repository

import com.app.lovelyprints.data.api.OrderApi
import com.app.lovelyprints.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class OrderRepository(
    private val orderApi: OrderApi
) {

    suspend fun createOrder(
        shopId: String,
        description: String,
        orientation: String,
        isUrgent: Boolean
    ): Result<CreateOrderResponse> {
        return try {
            val response = orderApi.createOrder(
                CreateOrderRequest(
                    shopId = shopId,
                    description = description,
                    orientation = orientation,
                    isUrgent = isUrgent
                )
            )
            if (response.isSuccessful && response.body() != null)
                Result.Success(response.body()!!)
            else
                Result.Error(response.message() ?: "Failed to create order")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun uploadFile(file: File): Result<UploadResponse> {
        return try {
            val requestFile =
                file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val body =
                MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = orderApi.uploadFile(body)
            if (response.isSuccessful && response.body() != null)
                Result.Success(response.body()!!)
            else
                Result.Error(response.message() ?: "Failed to upload file")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun attachDocument(
        orderId: String,
        fileKey: String,
        fileName: String,
        pageCount: Int,
        copies: Int,
        paperTypeId: String,
        colorModeId: String,
        finishTypeId: String
    ): Result<Unit> {
        return try {
            val response = orderApi.attachDocument(
                orderId,
                AttachDocumentRequest(
                    fileKey = fileKey,
                    fileName = fileName,
                    pageCount = pageCount,
                    copies = copies,
                    paperTypeId = paperTypeId,
                    colorModeId = colorModeId,
                    finishTypeId = finishTypeId
                )
            )
            if (response.isSuccessful)
                Result.Success(Unit)
            else
                Result.Error(response.message() ?: "Failed to attach document")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getOrders(): Result<OrdersResponse> {
        return try {
            val response = orderApi.getOrders()

            if (!response.isSuccessful) {
                return Result.Error("HTTP ${response.code()}")
            }

            val body = response.body()
                ?: return Result.Error("Empty response")

            Result.Success(body)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.Error(e.message ?: "Unknown error")
        }
    }


    suspend fun createPayment(orderId: String): Result<CreatePaymentResponse> {
        return try {
            val response = orderApi.createPayment(
                CreatePaymentRequest(orderId = orderId)
            )
            if (response.isSuccessful && response.body() != null)
                Result.Success(response.body()!!)
            else
                Result.Error(response.message() ?: "Failed to create payment")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun verifyPayment(
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String,
        orderId: String
    ): Result<VerifyPaymentResponse> {
        return try {
            val response = orderApi.verifyPayment(
                VerifyPaymentRequest(
                    razorpayOrderId = razorpayOrderId,
                    razorpayPaymentId = razorpayPaymentId,
                    razorpaySignature = razorpaySignature,
                    orderId = orderId
                )
            )
            if (response.isSuccessful && response.body() != null)
                Result.Success(response.body()!!)
            else
                Result.Error(response.message() ?: "Payment verification failed")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
