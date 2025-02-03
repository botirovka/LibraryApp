package com.botirovka.libraryapp.mvvm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.State
import com.botirovka.libraryapp.mvp.Presenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

sealed interface BooksState {
    data object Idle : BooksState
    data object Loading : BooksState
    data object LoadingMore : BooksState
    data class Success(val books: List<Book>) : BooksState
    data class Error(val message: String) : BooksState
    data class BookUnavailable(val message: String) : BooksState
    data class AllBooksLoaded(val bool: Boolean) : BooksState
}

sealed interface BooksIntent {
    data object LoadInitialBooks : BooksIntent
    data object LoadCachedBooks : BooksIntent
    data class LoadMoreBooks(val query: String? = null) : BooksIntent
    data class SearchBooks(val query: String) : BooksIntent
    data class BorrowBook(val book: Book) : BooksIntent
    data class ReturnBook(val book: Book) : BooksIntent
    data class ChangeFavoriteStatus(val book: Book) : BooksIntent
    data object ClearBookUnavailableMessage : BooksIntent
    data object CreateNewBook : BooksIntent
    data object FetchAllBooks : BooksIntent
}


class BooksViewModelMVI : ViewModel() {
    private val _state = MutableStateFlow<BooksState>(BooksState.Idle)
    val state: StateFlow<BooksState> = _state.asStateFlow()
    private var currentBooks: List<Book> = emptyList()
    private var lastQuery: String = ""
    private val pageSize = 8
    private var isAllBooksLoaded = false
    private var isLoading = false
    private var currentJob: Job? = null

    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()

    init {
        processIntent(BooksIntent.LoadInitialBooks)
    }

    fun processIntent(intent: BooksIntent) {
        when (intent) {
            is BooksIntent.LoadInitialBooks -> loadInitialBooks()
            is BooksIntent.LoadCachedBooks -> loadCachedBooks()
            is BooksIntent.LoadMoreBooks -> loadMoreBooks(intent.query ?: lastQuery)
            is BooksIntent.SearchBooks -> searchBooks(intent.query)
            is BooksIntent.BorrowBook -> borrowBook(intent.book)
            is BooksIntent.ReturnBook -> returnBook(intent.book)
            is BooksIntent.ChangeFavoriteStatus -> changeBookFavoriteStatus(intent.book)
            is BooksIntent.ClearBookUnavailableMessage -> clearBookUnavailableMessage()
            is BooksIntent.CreateNewBook -> createNewBook()
            is BooksIntent.FetchAllBooks -> fetchAllBooks()
            else -> {}
        }
    }

    private fun loadCachedBooks() {
        Log.d("mydebugMVI", "loadcached $currentBooks")
       _state.update { BooksState.Success(currentBooks) }
    }


    private fun loadInitialBooks() {
        Log.d("mydebugMVI", "loadinitial $lastQuery")
        _state.update { BooksState.Loading }
        isLoading = true
        viewModelScope.launch {
            val result = fetchBooksForPage()
            if (result.isNullOrEmpty()) {
                _state.update { BooksState.Error("Empty book list") }
            } else {
                if(lastQuery.isEmpty()){
                currentBooks = emptyList()
                currentBooks = result.map { it.copy() }
                    Log.d("mydebugMVI", "loadinitial last query empty $lastQuery")
                    _state.update { BooksState.Success(result) }
                }
                Log.d("mydebugMVI", "loadinitial last query  $lastQuery")
            }
            isLoading = false
        }
    }


    private fun loadMoreBooks(query: String = lastQuery) {

        Log.d("mydebugMVI", "loadmore start query: $query")
        if (currentJob?.isActive == true) {
            Log.d("mydebugMVP", "loadmore waiting")
            currentJob?.cancel()
            loadMoreBooks(query)
            return
        }
        if(query != lastQuery){
            currentJob?.cancel()
            Log.d("mydebugMVI", "loadmore: new query")
            currentBooks = emptyList()
            isAllBooksLoaded = false
            lastQuery = query
            _state.update { BooksState.AllBooksLoaded(false) }
            Log.d("mydebugMVI", "loadmore: change state ")

            _state.update { BooksState.Loading }
        } else {
            _state.update { BooksState.LoadingMore }
        }

        isLoading = true
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val result = fetchBooksForPage(query)
            Log.d("mydebugMVI", "loadmore result: $result")
            if (result.isEmpty()) {
                isAllBooksLoaded = true
                _state.update { BooksState.AllBooksLoaded(true) }
            }
            if (currentJob?.isCancelled == true || query != lastQuery) {
                Log.d("mydebugMVI", "canceled")
                loadMoreBooks(lastQuery)
                return@launch
            }
            val updatedBooks = currentBooks + result
            Log.d("mydebugMVI", "updated $updatedBooks")
            currentBooks = emptyList()
            currentBooks = updatedBooks.map { it.copy() }

            Log.d("mydebugMVI", "State change")
            _state.update { BooksState.Success(currentBooks) }
            Log.d("mydebugMVI", "State ${_state.value}")
            _state.update { BooksState.Idle }
            Log.d("mydebugMVI", "State ${_state.value}")
            isLoading = false
        }
    }


    private suspend fun fetchBooksForPage(query: String = ""): List<Book> {
        val randomDelay = Random.nextLong(500, 2000)
        delay(randomDelay)
        return Library.getBooksPaginated(currentBooks.size, pageSize, query)
    }


    private fun fetchAllBooks() {
        val currentLastQuery = lastQuery
        currentJob?.cancel()
        _state.update { BooksState.AllBooksLoaded(false)}
        _state.update { BooksState.Loading }
        viewModelScope.launch {
            Log.d("mydebugMVI", "fetchAllBooks: $lastQuery")
            when (val libraryState = Library.getAllBooks(lastQuery)) {
                is State.Data -> {
                    if(currentLastQuery == lastQuery) {
                        _state.update { BooksState.AllBooksLoaded(true)}
                        isAllBooksLoaded = true
                        currentBooks = emptyList()
                        currentBooks = libraryState.data.map { it.copy() }
                        _state.update { BooksState.Success(libraryState.data) }
                    }
                }
                is State.Error -> _state.update { BooksState.Error(libraryState.message) }
                else -> _state.update { BooksState.Error("Unexpected error") }
            }
            isLoading = false
        }
    }

    private fun createNewBook() {
        viewModelScope.launch {
            val newBook = Library.createNewBook()
            Library.addBook(newBook)
            isAllBooksLoaded = false
            _state.update { BooksState.AllBooksLoaded(false) }
            loadMoreBooks(lastQuery)

        }
    }


    private fun searchBooks(query: String) {
        Log.d("mydebugMVI", "searchBooks: $query")
        if (isLoading) return
        _state.update { BooksState.Loading }
        _state.update { BooksState.Idle }
        viewModelScope.launch {
            val searchResult = Library.searchBooks(query)
            currentBooks = emptyList()
            currentBooks = searchResult.map { it.copy()}
            _state.update { BooksState.Success(searchResult) }
        }
    }

    private fun borrowBook(book: Book) {
        viewModelScope.launch {
            val isBorrowed = Library.borrowBook(book.title)
            if (isBorrowed) {
                val updatedBooks = currentBooks.map { currentBook ->
                    if (currentBook.title == book.title) {
                        currentBook.copy(borrowedCount = currentBook.borrowedCount + 1)
                    } else {
                        currentBook
                    }
                }
                currentBooks = emptyList()
                currentBooks = updatedBooks.map { it.copy()}
                _state.update { BooksState.Success(updatedBooks) }
            } else {
                _bookUnavailableChannel.send("Book '${book.title}' is not available")
            }
        }
    }


    private fun returnBook(book: Book) {
        viewModelScope.launch {
            val updatedBooks = currentBooks.map { currentBook ->
                if (currentBook.id == book.id && currentBook.borrowedCount > 0) {
                    currentBook.copy(borrowedCount = currentBook.borrowedCount - 1)
                } else {
                    currentBook
                }
            }
            currentBooks = emptyList()
            currentBooks = updatedBooks.map { it.copy()}
            _state.update { BooksState.Success(updatedBooks) }
        }
    }

    private  fun changeBookFavoriteStatus(book: Book) {
        val deferred = viewModelScope.async {
            Log.d("mydebugMVI", "book: ${book.isFavorite} ")
                val updatedBooks = currentBooks.map { currentBook ->
                    if (currentBook.id == book.id) {
                        currentBook.copy(isFavorite = !currentBook.isFavorite)
                    } else {
                        currentBook
                    }
                }
                currentBooks = emptyList()
                currentBooks = updatedBooks.map { it.copy()}

                _state.update { BooksState.Success(updatedBooks.toList()) }


            }
        viewModelScope.async {
            deferred.await()
            Library.addBookToFavorite(book.id)
        }
        }



    private fun clearBookUnavailableMessage() {
        _state.update { BooksState.Idle }
    }
}