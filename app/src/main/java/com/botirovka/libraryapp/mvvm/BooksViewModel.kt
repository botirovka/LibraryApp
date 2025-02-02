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
    private var currentPage = 0
    private var currentIndex = 0
    private val pageSize = 8

    init {
        if (isDataLoaded.not()) {
            loadInitialBooks()
        }
    }

    private fun loadInitialBooks(pagesToLoad: Int = 2) {
        _loadingLiveData.value = true
        _loadingMoreLiveData.value = false
        _errorLiveData.value = null
        viewModelScope.launch {
            Log.d("mydebug", "start loadInitialBooks: "  + currentPage)
            val startPage = currentPage
            currentPage += pagesToLoad
            loadBooksParallel(startPage until startPage + pagesToLoad)
        }
    }

    fun loadMoreBooks(pagesToLoad: Int = 2, currentIndex: Int = 0) {
        if (isLoading) return

        if(currentPage == 0){
        this.currentIndex = currentIndex
        }

        _loadingLiveData.value = false
        _loadingMoreLiveData.value = true
        _errorLiveData.value = null


        isLoading = true
        val startPage = currentPage
        currentPage += pagesToLoad

        viewModelScope.launch {
            try {
                Log.d("mydebugMVVM", "start load more Books: from $startPage to $currentPage")
                loadBooksParallel(startPage until startPage + pagesToLoad)
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadBooksParallel(pageRange: IntRange) {
        try {
            Log.d("mydebugMVVM", "start pageRange: $pageRange")
            Log.d("mydebugMVVM", "start loadBooksParallel: $currentPage")
            val booksResult = withContext(Dispatchers.IO) {
                val deferredPages = pageRange.map { page ->
                    async {
                        fetchBooksForPage(page)
                    }
                }
                val results = deferredPages.awaitAll()
                val emptyCount = results.count { it.isEmpty() }
                Log.d("mydebugMVVM", "empty: $emptyCount")
                results.flatten().also {

                    if(it.isEmpty() == true){
                        currentPage = 0
                        _isAllBookLoadedStateFlow.emit(true)
                    }

                    Log.d("mydebugMVVM", "end loadBooksParallel: $currentPage")
                }
            }
            if(_booksLiveData.value.isNullOrEmpty()){
                _booksLiveData.value = booksResult
            }
            else{
                _booksLiveData.value = _booksLiveData.value!!.plus(booksResult)
            }

            _loadingLiveData.value = false
            _loadingMoreLiveData.value = false

        } catch (e: Exception) {
            _errorLiveData.value = "Unexpected error"
            _loadingLiveData.value = false
        }
    }

    private suspend fun fetchBooksForPage(page: Int): List<Book> {
        val randomDelay = Random.nextLong(500, 2000)
        delay(randomDelay)
        return Library.getBooksPaginated(page, pageSize, currentIndex)
    }

    fun fetchBooks() {
        _loadingLiveData.value = true
        _errorLiveData.value = null
        viewModelScope.launch {
            when (val state = Library.getAllBooks()) {
                is State.Data -> {
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


}