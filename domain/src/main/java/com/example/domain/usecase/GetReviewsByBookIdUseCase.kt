package com.example.domain.usecase

import com.example.domain.model.Review
import com.example.domain.repository.BookRepository
import javax.inject.Inject

class GetReviewsByBookIdUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend operator fun invoke(bookId: Int): List<Review>{
        return bookRepository.getReviewsByBookId(bookId)
    }
}