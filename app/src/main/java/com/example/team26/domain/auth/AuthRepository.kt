package com.example.team26.domain.auth

interface AuthRepository {
    suspend fun login(email: String, password: String)
}