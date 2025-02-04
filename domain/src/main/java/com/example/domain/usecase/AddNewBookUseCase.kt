package com.example.domain.usecase

import com.example.domain.DIReplacer
import com.example.domain.model.Book


class AddNewBookUseCase() {
    operator fun invoke(book: Book): Int {
        return DIReplacer.bookRepository.addBook(book)
    }
}