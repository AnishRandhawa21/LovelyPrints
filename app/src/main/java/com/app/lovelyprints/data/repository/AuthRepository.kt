package com.app.lovelyprints.data.repository

import android.util.Log
import com.app.lovelyprints.core.auth.TokenManager
import com.app.lovelyprints.data.api.AuthApi
import com.app.lovelyprints.data.model.LoginRequest
import com.app.lovelyprints.data.model.LoginResponse
import com.app.lovelyprints.data.model.SignupRequest

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {

    /* ---------------- LOGIN ---------------- */

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 -> Result.Error("Invalid email or password")
                    500 -> Result.Error("Server error. Please try again later.")
                    else -> Result.Error("Login failed. Please try again.")
                }
            }

            val body = response.body()
                ?: return Result.Error("Empty server response")

            val token = body.data.session.access_token
            Log.d("AUTH", "SAVING TOKEN = $token")

            // ✅ SAVE TOKEN
            tokenManager.saveToken(token)

            tokenManager.saveUserInfo(
                userId = body.data.user.id,
                userName = body.data.user.user_metadata.name
                    ?: email.substringBefore("@"),
                role = body.data.user.user_metadata.role
            )

            Result.Success(body)

        } catch (e: Exception) {

            val message = when {
                e.message?.contains("Unable to resolve host", true) == true ->
                    "Cannot connect to server. Check your internet connection."

                e.message?.contains("timeout", true) == true ->
                    "Server is taking too long to respond."

                else ->
                    "Something went wrong. Please try again."
            }

            Result.Error(message)
        }
    }

    /* ---------------- SIGNUP ---------------- */

    suspend fun signup(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val response = authApi.signup(
                SignupRequest(name, email, password)
            )

            if (!response.isSuccessful) {
                return when (response.code()) {
                    409 -> Result.Error("Account already exists with this email.")
                    400 -> Result.Error("Invalid signup data.")
                    500 -> Result.Error("Server error. Please try again later.")
                    else -> Result.Error("Signup failed. Please try again.")
                }
            }

            Result.Success(Unit)

        } catch (e: Exception) {

            val message = when {
                e.message?.contains("Unable to resolve host", true) == true ->
                    "Cannot connect to server. Check your internet connection."

                e.message?.contains("timeout", true) == true ->
                    "Server is taking too long to respond."

                else ->
                    "Something went wrong. Please try again."
            }

            Result.Error(message)
        }
    }

    /* ---------------- LOGOUT ---------------- */

    suspend fun logout() {
        tokenManager.clearAll()
    }
}
