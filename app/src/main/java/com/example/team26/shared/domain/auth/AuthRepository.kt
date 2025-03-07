package com.example.team26.shared.domain.auth

interface AuthRepository {
    suspend fun login(email: String, password: String)
}