package com.example.domain.usecase

import com.example.domain.model.Book
import com.example.domain.repository.BookRepository
import javax.inject.Inject

class CreateNewBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(): Book {
        return bookRepository.createNewBook()
    }
}