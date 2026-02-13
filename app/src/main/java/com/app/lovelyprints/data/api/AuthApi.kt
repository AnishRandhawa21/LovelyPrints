package com.app.lovelyprints.data.api

import com.app.lovelyprints.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun signup(@Body request: SignupRequest): Response<Unit>

    @GET("auth/organisations")
    suspend fun getOrganisations(): Response<OrganisationResponse>

}