package com.example.domain.repository
import com.example.domain.model.Post

interface PostRepository {
    suspend fun getPosts(): Result<List<Post>>
    suspend fun getPostById(postId: Int): Result<Post>
    suspend fun getCommentsByPostId(postId: Int): Result<List<Post>>
    suspend fun createPost(post: Post): Result<Post>
    suspend fun updatePost(postId: Int, post: Post): Result<Post>
    suspend fun deletePost(postId: Int): Result<Unit>
}