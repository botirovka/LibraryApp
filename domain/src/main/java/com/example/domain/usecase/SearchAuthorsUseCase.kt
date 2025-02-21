package com.example.domain.usecase

import com.example.domain.model.Author
import com.example.domain.repository.BookRepository
import javax.inject.Inject


class SearchAuthorsUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(query: String): List<Author> {
        return bookRepository.searchAuthors(query)
    }
}