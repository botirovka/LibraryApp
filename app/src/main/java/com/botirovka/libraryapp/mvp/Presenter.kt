package com.botirovka.libraryapp.mvp

import android.util.Log
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay

import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

object Presenter {
    private var view: ShowBookView? = null
    private val presenterScope = CoroutineScope(Dispatchers.Main)

    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()

    var isAllBookLoaded: Boolean = false

    var isLoading: Boolean = false
    var isMoreBookLoading: Boolean  = false


    private var lastQuery: String = ""
    private var cachedBooks: List<Book> = emptyList()


    private val pageSize = 8

    private var currentJob: Job? = null



    private fun loadInitialBooks() {
        Log.d("mydebugMVP", "loadInitialBooks: $lastQuery")
        Log.d("mydebugMVP", "loadInitialBooks: $view")
        view?.showLoading(true)
        currentJob = presenterScope.launch {
            Log.d("mydebugMVP", "start coroutine")
            val result = fetchBooksForPage()
            if(result.isNullOrEmpty()){
               view?.showError("Empty Book List")
               view?.showLoading(false)
            }
            cachedBooks = result
            view?.showLoading(false)
            Log.d("mydebugMVP", "loadInitialBooks end: $view")
            view?.showBooks(cachedBooks)
        }
    }

    fun loadMoreBooks(query: String = lastQuery) {

        if (currentJob?.isActive == true) {
            Log.d("mydebugMVP", "loadmore waiting")
            currentJob?.cancel()
            loadMoreBooks(query)
            return
        }

        if(query != lastQuery){
            currentJob?.cancel()
            Log.d("mydebugMVP", "loadmore new query")
            cachedBooks = emptyList()
            view?.showLoading(true)
            lastQuery = query
        }

        else{
            Log.d("mydebugMVP", "loadmore old query")
            isMoreBookLoading = true
            view?.showMoreLoading(true)
        }
        isLoading = true
        currentJob?.cancel()
        currentJob = presenterScope.launch {

                Log.d("mydebugMVP", "loadmore get books")
                val result = fetchBooksForPage(query)
                if(result.isEmpty()){
                   isAllBookLoaded = true
                    Log.d("mydebugMVP", "IsAllbookLoaded in Presenter $isAllBookLoaded")
                    view?.showMoreLoading(false)
                }
                cachedBooks =  cachedBooks.plus(result)
                if (isMoreBookLoading) {
                    view?.showMoreLoading(false)
                    isMoreBookLoading = false
                }
                Log.d("mydebugMVP", "loadmore turn off loading")
                view?.showLoading(false)
                isLoading = false
                view?.showBooks(cachedBooks)

        }
    }

    private suspend fun fetchBooksForPage(query: String = ""): List<Book> {
        val randomDelay = Random.nextLong(500, 2000)
        delay(randomDelay)
        return Library.getBooksPaginated(cachedBooks.size , pageSize, query)
    }

    fun addNewBook(newBook: Book) {
        presenterScope.launch {
            isAllBookLoaded = false
            Library.addBook(newBook)
        }
    }

    fun attachView(view: ShowBookView) {
        Log.d("mydebugMVP", "attach")
        this.view = view
        if(cachedBooks.isNotEmpty() ){
            view.showBooks(cachedBooks)
        }
    }

    fun detachView() {
        Log.d("mydebugMVP", "detach")
        this.view = null

    }

    fun reset(view: ShowBookView) {
        Log.d("mydebugMVP", "reset")
        lastQuery = ""
        cachedBooks = emptyList()
        isAllBookLoaded = false
        isLoading = false
        isMoreBookLoading = false
        currentJob?.cancel()
        Log.d("mydebugMVP", "attach from reset")
        this.view = view
        if(currentJob?.isActive == false || currentJob == null){
            Log.d("mydebugMVP", "reset $view")
            Log.d("mydebugMVP", "reset job not active")
            loadInitialBooks()
        }
        else{
            Log.d("mydebugMVP", "reset job active")
        }
    }

    fun fetchBooks() {
        val currentLastQuery = lastQuery
        currentJob?.cancel()
        isAllBookLoaded = true
        view?.showLoading(true)
        presenterScope.launch {
            val state = Library.getAllBooks(lastQuery)
            withContext(Dispatchers.Main) {
                view?.showLoading(false)
                when (state) {
                    is State.Data -> {
                        if(currentLastQuery == lastQuery) {
                            cachedBooks = state.data
                            view?.showBooks(state.data)
                        }
                        else{
                            Log.d("mydebugMVP", "Query not equal")
                        }
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
            val isChanged = Library.addBookToFavorite(book.id)
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