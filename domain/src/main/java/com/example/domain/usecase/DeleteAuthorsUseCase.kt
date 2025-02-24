package com.example.domain.usecase

import com.example.domain.model.Author
import com.example.domain.repository.BookRepository
import javax.inject.Inject

class DeleteAuthorsUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(authorsId: Set<Int>): Boolean {
        return bookRepository.deleteAuthors(authorsId)
    }
}