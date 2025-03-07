package com.example.team26.data.auth

import com.example.team26.domain.auth.AuthRepository

class AuthRepositoryImpl(
    private val api: AuthApi
): AuthRepository {

    override suspend fun login(email: String, password: String) {
        try {
            println("Logging in...")
            val request = LoginRequest(email, password)
            val response = api.login(request)
            println("Response: $response")
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}