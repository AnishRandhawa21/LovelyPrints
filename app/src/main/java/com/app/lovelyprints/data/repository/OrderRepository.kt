package com.app.lovelyprints.data.repository

import com.app.lovelyprints.data.api.OrderApi
import com.app.lovelyprints.data.model.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class OrderRepository(
    private val orderApi: OrderApi
) {

    /* ---------------- CREATE ORDER ---------------- */

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

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    500 ->
                        Result.Error("Server error. Please try again later.")

                    else ->
                        Result.Error("Failed to create order.")
                }
            }

            val body = response.body()
                ?: return Result.Error("Empty server response")

            Result.Success(body.data)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- UPLOAD FILE ---------------- */

    suspend fun uploadFile(file: File): Result<UploadData> {
        return try {
            val requestFile =
                file.asRequestBody("application/pdf".toMediaTypeOrNull())

            val body =
                MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = orderApi.uploadFile(body)

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    413 ->
                        Result.Error("File too large.")

                    500 ->
                        Result.Error("Server error while uploading file.")

                    else ->
                        Result.Error("File upload failed.")
                }
            }

            val data = response.body()?.data
                ?: return Result.Error("Empty upload response")

            Result.Success(data)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- ATTACH DOCUMENT ---------------- */

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

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    500 ->
                        Result.Error("Server error. Please try again later.")

                    else ->
                        Result.Error("Failed to attach document.")
                }
            }

            Result.Success(Unit)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- GET ORDERS ---------------- */

    suspend fun getOrders(): Result<OrdersResponse> {
        return try {
            val response = orderApi.getOrders()

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    500 ->
                        Result.Error("Server error. Please try again later.")

                    else ->
                        Result.Error("Failed to load orders.")
                }
            }

            val body = response.body()
                ?: return Result.Error("Empty response")

            Result.Success(body)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- PAYMENT ---------------- */

    suspend fun createPayment(orderId: String): Result<CreatePaymentResponse> {
        return try {
            val response = orderApi.createPayment(
                CreatePaymentRequest(orderId)
            )

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    500 ->
                        Result.Error("Server error. Please try again later.")

                    else ->
                        Result.Error("Payment creation failed.")
                }
            }

            Result.Success(response.body()!!)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
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
                    razorpayOrderId,
                    razorpayPaymentId,
                    razorpaySignature,
                    orderId
                )
            )

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    500 ->
                        Result.Error("Payment verification failed.")

                    else ->
                        Result.Error("Payment verification failed.")
                }
            }

            Result.Success(response.body()!!)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- ERROR MAPPER ---------------- */

    private fun mapNetworkError(e: Exception): String {
        return when (e) {
            is UnknownHostException ->
                "No internet connection."

            is SocketTimeoutException ->
                "Connection timed out. Please try again."

            is IOException ->
                "Network error. Please check your connection."

            else ->
                "Something went wrong. Please try again."
        }
    }
}
