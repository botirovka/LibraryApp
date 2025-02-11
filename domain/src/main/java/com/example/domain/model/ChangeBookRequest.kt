package com.example.domain.model

data class ChangeBookRequest(
    val id: Int,
    val title: String?,
    val author: String?,
    val genre: String?,
    val isFavorite: Boolean)
