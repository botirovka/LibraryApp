package com.botirovka.libraryapp.apiUI

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Post
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {


    private val _apiResult = MutableLiveData<Result<Any>>(Result.success(""))
    val apiResult: MutableLiveData<Result<Any>> = _apiResult

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> get() = _loadingLiveData

    fun getPosts() {
        viewModelScope.launch {
            _loadingLiveData.value = true
            _apiResult.value = postRepository.getPosts()
            _loadingLiveData.value = false
        }
    }

    fun getPostById(postId: Int) {
        viewModelScope.launch {
            _loadingLiveData.value = true
            _apiResult.value = postRepository.getPostById(postId)
            _loadingLiveData.value = false
        }
    }

    fun getCommentsByPostId(postId: Int) {
        viewModelScope.launch {
            _loadingLiveData.value = true
            _apiResult.value = postRepository.getCommentsByPostId(postId)
            _loadingLiveData.value = false
        }
    }

    fun createPost() {
        viewModelScope.launch {
            _loadingLiveData.value = true
            val newPost = Post(userId = 1, title = "New Post Title", body = "New post body content")
            _apiResult.value = postRepository.createPost(newPost)
            _loadingLiveData.value = false
        }
    }

    fun updatePost(postId: Int) {
        viewModelScope.launch {
            _loadingLiveData.value = true
            val updatedPost = Post(userId = 1, id = postId, title = "Updated Post Title", body = "Updated post body content")
            _apiResult.value = postRepository.updatePost(postId, updatedPost)
            _loadingLiveData.value = false
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            _loadingLiveData.value = true
            _apiResult.value = postRepository.deletePost(postId).map { "Post deleted successfully" }
            _loadingLiveData.value = false
        }
    }

    fun login(username: String, password: String){
        viewModelScope.launch {
            _loadingLiveData.value = true
            _apiResult.value = authRepository.login(username, password)
            _loadingLiveData.value = false
        }
    }

    fun getProfileInfo() {
        viewModelScope.launch {
            _loadingLiveData.value = true
            _apiResult.value = authRepository.getProfileInfo()
            _loadingLiveData.value = false
        }
    }

    fun logOut() {
        _loadingLiveData.value = true
        sharedPreferences.edit().putString("accessToken", "").apply()
        _loadingLiveData.value = false
    }

}