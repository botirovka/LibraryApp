package com.example.domain.usecase

import com.example.domain.model.Author
import com.example.domain.repository.BookRepository
import javax.inject.Inject

class DeleteBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(booksId: Set<Int>): Boolean {
        return bookRepository.deleteBooks(booksId)
    }
}