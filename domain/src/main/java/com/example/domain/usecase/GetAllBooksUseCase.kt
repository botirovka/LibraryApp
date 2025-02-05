package com.example.domain.usecase

import com.example.domain.model.State
import com.example.domain.repository.BookRepository
import javax.inject.Inject


class GetAllBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(query: String = ""): State {
        return bookRepository.getAllBooks(query)
    }
}