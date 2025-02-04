package com.example.domain.usecase

import com.example.domain.DIReplacer
import com.example.domain.model.Book


class SearchBooksUseCase() {
    suspend operator fun invoke(query: String): List<Book> {
        return DIReplacer.bookRepository.searchBooks(query)
    }
}