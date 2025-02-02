package com.botirovka.libraryapp.mvvm

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class BooksViewModel : ViewModel() {

    private val _booksLiveData = MutableLiveData<List<Book>>()
    val booksLiveData: LiveData<List<Book>> get() = _booksLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> get() = _loadingLiveData

    private val _loadingMoreLiveData = MutableLiveData<Boolean>()
    val loadingMoreLiveData: LiveData<Boolean> get() = _loadingMoreLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> get() = _errorLiveData

    private val _isAllBookLoadedStateFlow = MutableStateFlow(false)
    val isAllBookLoadedStateFlow = _isAllBookLoadedStateFlow.asStateFlow()

    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()
    private var isDataLoaded = false
    private var isLoading: Boolean = false

    private var lastQuery: String = ""
    private val pageSize = 8

    private var currentJob: Job? = null

    init {
        if (isDataLoaded.not()) {
            loadInitialBooks()
        }
    }

    private fun loadInitialBooks() {
        Log.d("mydebugPag", "loadInitialBooks: $lastQuery")
        _loadingLiveData.value = true
        _loadingMoreLiveData.value = false
        _errorLiveData.value = null
        viewModelScope.launch {
            val result = fetchBooksForPage()
            if(result.isNullOrEmpty()){
                _errorLiveData.value = "Empty book list"
                _loadingLiveData.value = false
            }
            _booksLiveData.value = result
            _loadingLiveData.value = false
        }
    }

    fun loadMoreBooks(query: String = lastQuery) {
        if (isLoading && currentJob?.isActive == true) {
            currentJob?.cancel()
            loadMoreBooks(query)
            return
        }
        Log.d("mydebugPag", "loadMoreBooks LastQuery: $lastQuery")
        Log.d("mydebugPag", "loadMoreBooks Query: $query")
        if(query != lastQuery){
            _booksLiveData.value = emptyList()
            _loadingLiveData.value = true
            _loadingMoreLiveData.value = false
            _errorLiveData.value = null
            lastQuery = query
        }

        else{
            _loadingLiveData.value = false
            _loadingMoreLiveData.value = true
            _errorLiveData.value = null
        }
        isLoading = true
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            try {
                val result = fetchBooksForPage(query)
                if(result.isEmpty()){
                    _isAllBookLoadedStateFlow.emit(true)
                    _loadingMoreLiveData.value = false
                }
                _booksLiveData.value = _booksLiveData.value?.plus(result)
            } finally {
                _loadingMoreLiveData.value = false
                _loadingLiveData.value = false
                isLoading = false
            }
        }
    }


    private suspend fun fetchBooksForPage(query: String = ""): List<Book> {
        val randomDelay = Random.nextLong(500, 2000)
        delay(randomDelay)
        return Library.getBooksPaginated(_booksLiveData.value?.size ?: 0, pageSize, query)
    }

    fun fetchBooks() {
        if(_isAllBookLoadedStateFlow.value){
            viewModelScope.launch {
                _bookUnavailableChannel.send("All books already loaded")
            }
            return
        }
        _loadingLiveData.value = true
        _errorLiveData.value = null
        viewModelScope.launch {
            when (val state = Library.getAllBooks(lastQuery)) {
                is State.Data -> {
                    _isAllBookLoadedStateFlow.emit(true)
                    _booksLiveData.value = state.data
                    _loadingLiveData.value = false
                }
                is State.Error -> {
                    _errorLiveData.value = state.message
                    _loadingLiveData.value = false
                }
                else -> {
                    _errorLiveData.value = "Unexpected error"
                    _loadingLiveData.value = false
                }
            }
        }
    }

    fun addNewBook(newBook: Book) {
        viewModelScope.launch {
        Library.addBook(newBook)
        _isAllBookLoadedStateFlow.emit(false)
        }
    }

    fun searchBooks(query: String) {
        _loadingLiveData.value = true
        _loadingMoreLiveData.value = false
        _errorLiveData.value = null
        viewModelScope.launch {
            val searchResult = Library.searchBooks(query)
            _booksLiveData.value = searchResult
            _loadingLiveData.value = false
        }
    }

    fun borrowBook(book: Book) {
        viewModelScope.launch {
            val isBorrowed = Library.borrowBook(book.title)
            if (isBorrowed) {
                _booksLiveData.value = _booksLiveData.value

            } else {
                _bookUnavailableChannel.send("Book '${book.title}' is not available")
            }
        }
    }

    fun returnBook(book: Book) {
        viewModelScope.launch {
            val updatedBooks = _booksLiveData.value?.map {
                if (it.id == book.id && it.borrowedCount > 0) {
                    it.copy(borrowedCount = it.borrowedCount - 1)
                } else it
            } ?: return@launch

            _booksLiveData.value = updatedBooks
        }
    }

    fun changeBookFavoriteStatus(book: Book) {
        viewModelScope.launch {
            val isChanged = Library.addBookToFavorite(book.title)
            if (isChanged) {
                _booksLiveData.value = _booksLiveData.value

            } else {
                _errorLiveData.value = "Unexpected error"
            }
        }
    }


}