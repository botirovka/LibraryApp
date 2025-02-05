package com.example.domain.usecase

import com.example.domain.repository.BookRepository
import javax.inject.Inject


class ToggleFavoriteBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(id: Int): Boolean {
        return bookRepository.addBookToFavorite(id)
    }
}