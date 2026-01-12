package com.app.lovelyprints.data.model

// ---------- REQUESTS ----------

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "student"
)

// ---------- RESPONSES ----------

data class LoginResponse(
    val data: LoginData
)

data class LoginData(
    val user: User,
    val session: Session
)

data class Session(
    val access_token: String
)

// ---------- USER ----------

data class User(
    val id: String,
    val user_metadata: UserMetadata
)

data class UserMetadata(
    val role: String,
    val name: String? = null
)
