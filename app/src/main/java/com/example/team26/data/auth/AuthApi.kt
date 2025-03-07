package com.example.team26.data.auth

import retrofit2.http.Body
import retrofit2.http.POST

// Todo: Finish adding needed functions (this is just a quick mockup)

interface AuthApi {

    @POST("login")
    suspend fun login(
        @Body body: LoginRequest
    )
}

data class LoginRequest(
    val email: String,
    val password: String
)