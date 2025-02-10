package com.example.domain.model

data class Book(
    var id: Int = 0,
    var title: String,
    var author: String,
    var genre: Genres,
    val image: String = "",
    var borrowedCount: Int = 0,
    var totalBookCount: Int = 0,
    val isAvailable: Boolean = true,
    var lastBorrowedTime: Long? = null,
    var isFavorite: Boolean = false

)