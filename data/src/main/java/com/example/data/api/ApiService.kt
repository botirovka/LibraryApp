package com.example.data.api

import com.example.domain.model.PostDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/posts")
    suspend fun getPosts(): Response<List<PostDto>>

    @GET("/posts/{id}")
    suspend fun getPostById(@Path("id") postId: Int): Response<PostDto>

    @GET("/comments")
    suspend fun getCommentsByPostId(@Query("postId") postId: Int): Response<List<PostDto>>

    @POST("/posts")
    suspend fun createPost(@Body post: PostDto): Response<PostDto>

    @PUT("/posts/{id}")
    suspend fun updatePost(@Path("id") postId: Int, @Body post: PostDto): Response<PostDto>

    @DELETE("/posts/{id}")
    suspend fun deletePost(@Path("id") postId: Int): Response<ResponseBody>
}