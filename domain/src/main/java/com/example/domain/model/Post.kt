package com.example.domain.model

data class Post(
    val userId: Int? = null,
    val id: Int? = null,
    val title: String =  "",
    val body: String = ""
)