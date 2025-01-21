package com.botirovka.libraryapp.models

data class Book (
    val title: String,
    val author: String,
    val genre: Genres,
    val image: String = "",
    var isBorrowed: Boolean = false
)