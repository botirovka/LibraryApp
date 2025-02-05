package com.botirovka.libraryapp.mvvm


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.domain.model.Book
import com.example.domain.model.State
import com.example.domain.usecase.AddNewBookUseCase
import com.example.domain.usecase.BorrowBookUseCase
import com.example.domain.usecase.CreateNewBookUseCase
import com.example.domain.usecase.GetAllBooksUseCase
import com.example.domain.usecase.GetPaginatedBooksUseCase
import com.example.domain.usecase.SearchBooksUseCase
import com.example.domain.usecase.ToggleFavoriteBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val getPaginatedBooksUseCase: GetPaginatedBooksUseCase,
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val borrowBookUseCase: BorrowBookUseCase,
    private val toggleFavoriteBookUseCase: ToggleFavoriteBookUseCase,
    private val addNewBookUseCase: AddNewBookUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val createNewBookUseCase: CreateNewBookUseCase
) : ViewModel() {

    private val _booksLiveData = MutableLiveData<List<Book>>(emptyList())
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
        currentJob = viewModelScope.launch {
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
        if (isLoading) {
            lastQuery = query
        }
        _errorLiveData.value = null

        if(query != lastQuery){
            currentJob?.cancel()
            _booksLiveData.value = emptyList()
            viewModelScope.launch {
                _isAllBookLoadedStateFlow.emit(false)
            }
            _loadingLiveData.value = true
            _loadingMoreLiveData.value = false
            lastQuery = query
        }

        else{
            _loadingLiveData.value = false
            _loadingMoreLiveData.value = true
        }

        isLoading = true
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val result = fetchBooksForPage(query)
            if(result.isEmpty()){
                _isAllBookLoadedStateFlow.emit(true)
                _loadingMoreLiveData.value = false
            }
            if(query != lastQuery){
                loadMoreBooks(lastQuery)
                return@launch
            }

            _booksLiveData.value = _booksLiveData.value?.plus(result)
            _loadingMoreLiveData.value = false
            _loadingLiveData.value = false
            isLoading = false

        }
    }


    private suspend fun fetchBooksForPage(query: String = ""): List<Book> {
        return getPaginatedBooksUseCase(_booksLiveData.value?.size ?: 0, pageSize, query)
    }

    fun fetchBooks() {
        val currentLastQuery = lastQuery
        if(_isAllBookLoadedStateFlow.value){
            viewModelScope.launch {
                _bookUnavailableChannel.send("All books already loaded")
            }
            return
        }
        _loadingLiveData.value = true
        _errorLiveData.value = null

        viewModelScope.launch {
            when (val state = getAllBooksUseCase(lastQuery)) {

                is State.Data -> {
                    if(currentLastQuery == lastQuery){
                        _isAllBookLoadedStateFlow.emit(true)
                        _booksLiveData.value = state.data
                        _loadingLiveData.value = false
                    }
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

    fun createNewBook(){
        val newBook  = createNewBookUseCase()
        addNewBook(newBook)
    }

    fun addNewBook(newBook: Book) {
        viewModelScope.launch {
            addNewBookUseCase(newBook)
            _isAllBookLoadedStateFlow.emit(false)
        }
    }

    fun searchBooks(query: String) {
        _loadingLiveData.value = true
        _loadingMoreLiveData.value = false
        _errorLiveData.value = null
        viewModelScope.launch {
            val searchResult = searchBooksUseCase(query)
            _booksLiveData.value = searchResult
            _loadingLiveData.value = false
        }
    }

    fun borrowBook(book: Book) {
        viewModelScope.launch {
            val isBorrowed = borrowBookUseCase(book.title)
            if (isBorrowed) {
                _booksLiveData.value = _booksLiveData.value

            } else {
                _bookUnavailableChannel.send("Book '${book.title}' is not available")
            }
        }
    }

    fun returnBook(book: Book) {

    }

    fun changeBookFavoriteStatus(book: Book) {
        viewModelScope.launch {
            val isChanged = toggleFavoriteBookUseCase(book.id)
            if (isChanged) {
                _booksLiveData.value = _booksLiveData.value

            } else {
                _errorLiveData.value = "Unexpected error"
            }
        }
    }




}