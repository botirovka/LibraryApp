package com.example.domain.usecase

import com.example.domain.DIReplacer
import com.example.domain.model.State


class GetAllBooksUseCase() {
    suspend operator fun invoke(query: String = ""): State {
        return DIReplacer.bookRepository.getAllBooks(query)
    }
}