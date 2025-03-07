package com.example.data.mapper

import com.example.domain.model.Post
import com.example.domain.model.PostDto
import com.example.domain.model.Profile
import com.example.domain.model.ProfileDto

fun PostDto.toDomainPost(): Post {
    return Post(
        userId = userId,
        id = id,
        title = title,
        body = body
    )
}

fun Post.toDataPostDto(): PostDto {
    return PostDto(
        userId = userId,
        id = id,
        title = title,
        body = body
    )
}

fun ProfileDto.toDomainProfile(): Profile{
    return Profile(
        id = id,
        username = username,
        email = email,
        gender = gender,
        image = image
    )
}
