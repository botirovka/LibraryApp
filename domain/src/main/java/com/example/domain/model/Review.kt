package com.example.domain.model

data class Review(
    var id: Int = 0,
    val bookId: Int = 0,
    val username: String,
    val rating: Float,
    val reviewText: String
)
