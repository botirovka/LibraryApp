package com.botirovka.libraryapp.mvp

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Presenter {
    private var view: ShowBookView? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main)
    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()
    private var lastQuery: String = ""
    private var cachedBooks: List<Book> = emptyList()

    fun attachView(view: ShowBookView) {
        this.view = view
        if (cachedBooks.isNotEmpty()) {
            view.showBooks(cachedBooks)
            return
        }
    }

    fun detachView() {
        this.view = null

    }

    fun reset() {
        lastQuery = ""
        cachedBooks = emptyList()
    }

    fun fetchBooks() {
        view?.showLoading(true)
        presenterScope.launch {
            val state = Library.getAllBooks()
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                when (state) {
                    is State.Data -> {
                        cachedBooks = state.data
                        view?.showBooks(state.data)
                    }

                    is State.Error -> view?.showError(state.message)
                    else -> view?.showError("Unexpected error")
                }
            }
        }
    }

    fun searchBooks(query: String) {
        Log.d("mydebug", "LAST: $lastQuery NEW: $query")
        if (query == lastQuery) {
            Log.d("mydebug", "setupSearch: cancel")
            return
        }
        lastQuery = query
        Log.d("mydebug", "setupSearch: search")
        view?.showLoading(true)
        presenterScope.launch {
            val searchResult = Library.searchBooks(query)
            cachedBooks = searchResult
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                view?.showBooks(searchResult)
            }
        }
    }

    fun borrowBook(book: Book) {
        presenterScope.launch {
            val currentBooks = view?.getCurrentBooks() ?: emptyList()
            val isBorrowed = Library.borrowBook(book.title)
            if (isBorrowed.not()) {
                _bookUnavailableChannel.send("Book '${book.title}' is not available")
            }
            // перенести у вью
            withContext(Dispatchers.Main) {
                view?.showBooks(currentBooks)
            }
        }
    }


    fun returnBook(book: Book) {
        presenterScope.launch {
            val currentBooks = view?.getCurrentBooks() ?: emptyList()
            val updatedBooks = currentBooks.map {
                if (it.id == book.id && it.borrowedCount > 0) {
                    it.copy(borrowedCount = it.borrowedCount - 1)
                } else it
            }

            withContext(Dispatchers.Main) {
                view?.showBooks(updatedBooks)
            }
        }
    }

    fun changeBookFavoriteStatus(book: Book) {
        presenterScope.launch {
            val isChanged = Library.addBookToFavorite(book.title)
            val currentBooks = view?.getCurrentBooks() ?: emptyList()
            if (isChanged) {
                withContext(Dispatchers.Main) {
                    view?.showBooks(currentBooks)
                }

            } else {
                view?.showError("Unexpected error")
            }
        }
    }
}