package com.example.domain.usecase

import com.example.domain.DIReplacer
import com.example.domain.model.Book


class GetPaginatedBooksUseCase() {
    suspend operator fun invoke(startIndex: Int, pageSize: Int, query: String): List<Book> {
        return DIReplacer.bookRepository.getBooksPaginated(startIndex, pageSize, query)
    }
}