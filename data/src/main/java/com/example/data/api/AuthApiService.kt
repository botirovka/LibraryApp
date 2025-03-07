package com.example.data.api

import com.example.data.model.AuthRequest
import com.example.domain.model.ProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<ProfileDto>

    @GET("/auth/me")
    suspend fun getProfileInfo(): Response<ProfileDto>
}