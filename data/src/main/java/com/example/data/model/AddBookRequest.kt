package com.example.data.model

import com.example.domain.model.Book
import com.example.domain.model.Genres

data class AddBookRequest (
    val title: String,
    val author: String,
    val genre: Genres,
    val image: String = "",
    var borrowedCount: Int = 0,
    var totalBookCount: Int = 0,
    val isAvailable: Boolean = true,
    var lastBorrowedTime: String? = null,
    var isFavorite: Boolean = false
)

fun Book.toAddBookRequest() = AddBookRequest(
    title, author, genre, image, borrowedCount, totalBookCount, isAvailable
)