package com.example.domain.usecase

import com.example.domain.model.ChangeBookRequest
import com.example.domain.repository.BookRepository
import javax.inject.Inject

class ChangeBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(changeBookRequest: ChangeBookRequest): Boolean {
        return bookRepository.changeBook(changeBookRequest)
    }
}