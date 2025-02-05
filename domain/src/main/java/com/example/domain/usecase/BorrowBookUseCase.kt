package com.example.domain.usecase

import com.example.domain.repository.BookRepository
import javax.inject.Inject


class BorrowBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(title: String): Boolean {
        return bookRepository.borrowBook(title)
    }
}