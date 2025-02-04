package com.example.domain.usecase

import com.example.domain.DIReplacer


class BorrowBookUseCase() {
    operator fun invoke(title: String): Boolean {
        return DIReplacer.bookRepository.borrowBook(title)
    }
}