package com.botirovka.libraryapp.mvp

import com.botirovka.libraryapp.models.Book

interface ShowBookView {
    fun showBooks(books: List<Book>)
    fun showError(message: String)
    fun showLoading(isLoading: Boolean)
    fun showMoreLoading(isMoreLoading: Boolean)
    fun showBookUnavailableMessage(message: String)
    fun onBorrowButtonClick(book: Book)
    fun onReturnButtonClick(book: Book)
    fun getCurrentBooks(): List<Book>
}