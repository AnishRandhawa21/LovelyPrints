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

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = authApi.login(LoginRequest(email, password))

            if (!response.isSuccessful) {
                return Result.Error("Login failed: ${response.code()}")
            }

            val body = response.body()
                ?: return Result.Error("Empty server response")

            val token = body.data.session.access_token
            Log.d("AUTH", "SAVING TOKEN = $token")

            // ✅ SAVE ONCE – source of truth
            tokenManager.saveToken(token)

            tokenManager.saveUserInfo(
                userId = body.data.user.id,
                userName = body.data.user.user_metadata.name
                    ?: email.substringBefore("@"),
                role = body.data.user.user_metadata.role
            )

            Result.Success(body)

        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun signup(
        name: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            val response = authApi.signup(
                SignupRequest(name, email, password)
            )

            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Signup failed: ${response.code()}")
            }

        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun logout() {
        tokenManager.clearAll()
    }
}
