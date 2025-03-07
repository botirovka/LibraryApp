package com.example.domain.repository

import com.example.domain.model.Profile

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Profile>
    suspend fun getProfileInfo() : Result<Profile>
}