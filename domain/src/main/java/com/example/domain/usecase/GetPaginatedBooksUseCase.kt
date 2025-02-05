package com.example.domain.usecase

import com.example.domain.model.Book
import com.example.domain.repository.BookRepository
import javax.inject.Inject


class GetPaginatedBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(startIndex: Int, pageSize: Int, query: String): List<Book> {
        return bookRepository.getBooksPaginated(startIndex, pageSize, query)
    }
}