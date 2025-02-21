package com.example.domain.model

data class Author(
    var id: Int = 0,
    var name: String,
    val image: String = "",
    var totalBooksCount: Int,
    var rating: Float
)


