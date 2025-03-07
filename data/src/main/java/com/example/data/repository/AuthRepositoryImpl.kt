package com.example.data.repository

import android.content.SharedPreferences
import com.example.data.api.AuthApiService
import com.example.data.mapper.toDomainProfile
import com.example.data.model.AuthRequest
import com.example.domain.model.Profile
import com.example.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val sharedPreferences: SharedPreferences
): AuthRepository {
    override suspend fun login(username: String, password: String): Result<Profile> {
        val result = safeApiCall { authApiService.login(AuthRequest(username, password)) }
        result.getOrNull()?.let { profile ->
           sharedPreferences.edit().putString("accessToken", profile.accessToken).apply()
        }
        return result.map { it.toDomainProfile() }
    }

    override suspend fun getProfileInfo(): Result<Profile> {
        return safeApiCall { authApiService.getProfileInfo() }.map { it.toDomainProfile() }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {

                Result.failure(Exception("API Error: ${response.code()} ${response.message()} \nCaused by\n: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network Error: ${e.message}"))
        }
    }
}