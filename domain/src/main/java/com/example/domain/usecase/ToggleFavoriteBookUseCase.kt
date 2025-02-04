package com.example.domain.usecase

import com.example.domain.DIReplacer


class ToggleFavoriteBookUseCase() {
    operator fun invoke(id: Int): Boolean {
        return DIReplacer.bookRepository.addBookToFavorite(id)
    }
}