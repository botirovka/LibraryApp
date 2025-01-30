package com.botirovka.libraryapp.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface BooksState {
    data object Loading : BooksState
    data class Success(val books: List<Book>) : BooksState
    data class Error(val message: String) : BooksState
    data class BookUnavailable(val message: String) : BooksState
    data object Idle : BooksState
}

sealed interface BooksIntent {
    data object FetchBooks : BooksIntent
    data class SearchBooks(val query: String) : BooksIntent
    data class BorrowBook(val book: Book) : BooksIntent
    data class ReturnBook(val book: Book) : BooksIntent
    data object ClearBookUnavailableMessage : BooksIntent
}


class BooksViewModelMVI : ViewModel() {
    private val _state = MutableStateFlow<BooksState>(BooksState.Idle)
    val state: StateFlow<BooksState> = _state.asStateFlow()
    private var currentBooks: List<Book> = emptyList()

    init {
        processIntent(BooksIntent.FetchBooks)
    }

    fun processIntent(intent: BooksIntent) {
        when (intent) {
            is BooksIntent.FetchBooks -> fetchBooks()
            is BooksIntent.SearchBooks -> searchBooks(intent.query)
            is BooksIntent.BorrowBook -> borrowBook(intent.book)
            is BooksIntent.ReturnBook -> returnBook(intent.book)
            is BooksIntent.ClearBookUnavailableMessage -> clearBookUnavailableMessage()
            else -> {}
        }
    }

    private fun fetchBooks() {
        _state.update { BooksState.Loading }
        viewModelScope.launch {
            val libraryState = Library.getAllBooks()
            withContext(Dispatchers.Main) {
                when (libraryState) {
                    is State.Data -> {
                        currentBooks = libraryState.data
                        _state.update { BooksState.Success(libraryState.data) }
                    }
                    is State.Error -> _state.update { BooksState.Error(libraryState.message) }
                    else -> _state.update { BooksState.Error("Unexpected error") }
                }
            }
        }
    }

    private fun searchBooks(query: String) {
        _state.update { BooksState.Loading }
        viewModelScope.launch {
            val searchResult = Library.searchBooks(query)
            withContext(Dispatchers.Main) {
                _state.update { BooksState.Success(searchResult) }
            }
        }
    }

    private fun borrowBook(book: Book) {
        viewModelScope.launch {
            if (book.totalBookCount - book.borrowedCount == 0) {
                _state.update { BooksState.BookUnavailable("Book '${book.title}' is not available") }
                return@launch
            }

            val updatedBooks = currentBooks.map {
                if (it.id == book.id) {
                    it.copy(borrowedCount = it.borrowedCount + 1)
                } else it
            }
            currentBooks = updatedBooks
            _state.update { BooksState.Success(updatedBooks) }
        }
    }


    private fun returnBook(book: Book) {
        viewModelScope.launch {
            val updatedBooks = currentBooks.map {
                if (it.id == book.id && it.borrowedCount > 0) {
                    it.copy(borrowedCount = it.borrowedCount - 1)
                } else it
            }
            currentBooks = updatedBooks
            _state.update { BooksState.Success(updatedBooks) }
        }
    }

    private fun clearBookUnavailableMessage() {
        _state.update { BooksState.Idle }
    }
}