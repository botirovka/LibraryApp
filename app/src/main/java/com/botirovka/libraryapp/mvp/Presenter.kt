package com.botirovka.libraryapp.mvp

import android.util.Log
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

    private var lastSearchData: Pair<String, List<Book>> = "" to emptyList()
    private var cachedBooks: List<Book> = emptyList()
    private var isDataLoaded = false



    fun attachView(view: ShowBookView) {
        this.view = view
        if (lastSearchData.second.isNotEmpty()){
            view.showBooks(lastSearchData.second)
            return
        }
        if (cachedBooks.isNotEmpty()) {
            view.showBooks(cachedBooks)
            return
        } else {
            isDataLoaded = false
            fetchBooks()
            return
        }
    }

    fun detachView() {

        this.view = null

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
                            lastSearchData = "" to emptyList()
                            view?.showBooks(state.data)
                        }
                        is State.Error -> view?.showError(state.message)
                        else -> view?.showError("Unexpected error")
                    }
                }
            }
    }

    fun searchBooks(query: String) {
        if (lastSearchData.first == query){
            view?.showBooks(lastSearchData.second)
            return
        }

        view?.showLoading(true)
        presenterScope.launch {
            val searchResult = Library.searchBooks(query)
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                view?.showBooks(searchResult)
                lastSearchData = query to searchResult
            }
        }
    }

    fun borrowBook(book: Book) {
        presenterScope.launch {
            val currentBooks = (view as? BooksMVPFragment)?.getCurrentBooks() ?: emptyList()
            val isBorrowed = Library.borrowBook(book.title)
            if(isBorrowed.not()){
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