package com.example.domain.model

import com.google.gson.annotations.SerializedName

data class PostDto(
    @SerializedName("userId") val userId: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String = "",
    @SerializedName("body") val body: String = ""
)