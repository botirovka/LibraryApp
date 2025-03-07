package com.example.data.repository

import com.example.data.api.ApiService
import com.example.data.mapper.toDataPostDto
import com.example.data.mapper.toDomainPost
import com.example.domain.model.Post
import com.example.domain.repository.PostRepository
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : PostRepository {
    override suspend fun getPosts(): Result<List<Post>> {
        return safeApiCall { apiService.getPosts() }.map { postDtoList -> postDtoList.map { it.toDomainPost() } }
    }

    override suspend fun getPostById(postId: Int): Result<Post> {
        return safeApiCall { apiService.getPostById(postId) }.map { it.toDomainPost() }
    }

    override suspend fun getCommentsByPostId(postId: Int): Result<List<Post>> {
        return safeApiCall { apiService.getCommentsByPostId(postId) }.map { postDtoList -> postDtoList.map { it.toDomainPost() } }
    }

    override suspend fun createPost(post: Post): Result<Post> {
        return safeApiCall { apiService.createPost(post.toDataPostDto()) }.map { it.toDomainPost() }
    }

    override suspend fun updatePost(postId: Int, post: Post): Result<Post> {
        return safeApiCall { apiService.updatePost(postId, post.toDataPostDto()) }.map { it.toDomainPost() }
    }

    override suspend fun deletePost(postId: Int): Result<Unit> {
        return safeApiCallWithoutBody { apiService.deletePost(postId) }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network Error: ${e.message}"))
        }
    }
}

private suspend fun safeApiCallWithoutBody(apiCall: suspend () -> retrofit2.Response<*>): Result<Unit> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
        }
    } catch (e: Exception) {
        Result.failure(Exception("Network Error: ${e.message}"))
    }
}