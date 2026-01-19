package com.app.lovelyprints.data.repository

import android.util.Log
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
        orientation: PrintOrientation,
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
                Log.e("ORDER_REPO", "Create order failed: ${response.code()} - ${response.errorBody()?.string()}")
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
            Log.e("ORDER_REPO", "Create order exception: ${e.message}", e)
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
                Log.e("ORDER_REPO", "Upload file failed: ${response.code()} - ${response.errorBody()?.string()}")
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
            Log.e("ORDER_REPO", "Upload file exception: ${e.message}", e)
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
                Log.e("ORDER_REPO", "Attach document failed: ${response.code()} - ${response.errorBody()?.string()}")
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
            Log.e("ORDER_REPO", "Attach document exception: ${e.message}", e)
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- GET ORDERS ---------------- */

    suspend fun getOrders(): Result<OrdersResponse> {
        return try {
            val response = orderApi.getOrders()

            if (!response.isSuccessful) {
                Log.e("ORDER_REPO", "Get orders failed: ${response.code()} - ${response.errorBody()?.string()}")
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
            Log.e("ORDER_REPO", "Get orders exception: ${e.message}", e)
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- PAYMENT ---------------- */

    suspend fun createPayment(orderId: String): Result<CreatePaymentResponse> {
        return try {
            Log.d("ORDER_REPO", "Creating payment for order: $orderId")

            val response = orderApi.createPayment(
                CreatePaymentRequest(orderId)
            )

            Log.d("ORDER_REPO", "Payment API response code: ${response.code()}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("ORDER_REPO", "Create payment failed: ${response.code()} - $errorBody")

                return when (response.code()) {
                    400 -> Result.Error("Invalid order. Error: $errorBody")
                    401, 403 -> Result.Error("Session expired. Please login again.")
                    404 -> Result.Error("Order not found.")
                    500 -> Result.Error("Server error. Please try again later.")
                    else -> Result.Error("Payment creation failed: $errorBody")
                }
            }

            val apiResponse = response.body()
            if (apiResponse == null || !apiResponse.success) {
                Log.e("ORDER_REPO", "Payment response body is null or success is false")
                return Result.Error(apiResponse?.message ?: "Empty payment response")
            }

            val body = apiResponse.data
            Log.d("ORDER_REPO", "📦 Data: $body")

            // 🔥 VALIDATE DATA
            if (body.id.isNullOrBlank()) {
                Log.e("ORDER_REPO", "❌ Razorpay order_id is null or empty!")
                return Result.Error("Invalid payment response: missing order ID")
            }

            if (body.amount == null || body.amount == 0) {
                Log.e("ORDER_REPO", "❌ Payment amount is null or zero!")
                return Result.Error("Invalid payment response: missing amount")
            }

            Log.d("ORDER_REPO", "✅ Payment created successfully: order_id=${body.id}, amount=${body.amount}")
            Result.Success(body)

        } catch (e: Exception) {
            Log.e("ORDER_REPO", "Create payment exception: ${e.message}", e)
            e.printStackTrace()
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
            Log.d("ORDER_REPO", "Verifying payment - Order: $orderId, Payment: $razorpayPaymentId")

            val response = orderApi.verifyPayment(
                VerifyPaymentRequest(
                    razorpayOrderId,
                    razorpayPaymentId,
                    razorpaySignature,
                    orderId
                )
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("ORDER_REPO", "Verify payment failed: ${response.code()} - $errorBody")

                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    500 ->
                        Result.Error("Payment verification failed.")

                    else ->
                        Result.Error("Payment verification failed: $errorBody")
                }
            }

            val body = response.body()
            if (body == null) {
                Log.e("ORDER_REPO", "Verify payment response body is null")
                return Result.Error("Empty verification response")
            }

            Log.d("ORDER_REPO", "Payment verified successfully")
            Result.Success(body)

        } catch (e: Exception) {
            Log.e("ORDER_REPO", "Verify payment exception: ${e.message}", e)
            e.printStackTrace()
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
                "Something went wrong: ${e.message}"
        }
    }
}