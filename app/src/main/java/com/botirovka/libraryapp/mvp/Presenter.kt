package com.botirovka.libraryapp.mvp

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

class Presenter {
    private var view: ShowBookView? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main)

    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()

    fun attachView(view: ShowBookView) {
        this.view = view
    }

    fun detachView() {
        this.view = null
        presenterScope.cancel()
    }

    fun fetchBooks() {
        view?.showLoading(true)
        presenterScope.launch {
            val state = Library.getAllBooks()
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                when (state) {
                    is State.Data -> view?.showBooks(state.data)
                    is State.Error -> view?.showError(state.message)
                    else -> view?.showError("Unexpected error")
                }
            }
        }
    }

    fun searchBooks(query: String) {
        view?.showLoading(true)
        presenterScope.launch {
            val searchResult = Library.searchBooks(query)
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                view?.showBooks(searchResult)
            }
        }
    }

    fun borrowBook(book: Book) {
        presenterScope.launch {
            val currentBooks = (view as? BooksMVPFragment)?.getCurrentBooks() ?: emptyList()
            val updatedBooks = currentBooks.map {
                if (it.id == book.id) {
                    if (it.totalBookCount - it.borrowedCount == 0) {
                        _bookUnavailableChannel.send("Book '${it.title}' is not available")
                        it
                    } else {
                        it.copy(borrowedCount = it.borrowedCount + 1)
                    }
                } else it
            }

            withContext(Dispatchers.Main) {
                view?.showBooks(updatedBooks)
            }
        }
    }


    fun returnBook(book: Book) {
        presenterScope.launch {
            val currentBooks = (view as? BooksMVPFragment)?.getCurrentBooks() ?: emptyList()
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
}