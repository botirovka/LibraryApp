package com.botirovka.libraryapp.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.State
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class BooksViewModel : ViewModel() {

    private val _booksLiveData = MutableLiveData<List<Book>>()
    val booksLiveData: LiveData<List<Book>> get() = _booksLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> get() = _loadingLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> get() = _errorLiveData

    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()

    init {
        fetchBooks()
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

    fun searchBooks(query: String) {
        _loadingLiveData.value = true
        _errorLiveData.value = null
        viewModelScope.launch {
            val searchResult = Library.searchBooks(query)
            _booksLiveData.value = searchResult
            _loadingLiveData.value = false
        }
    }

    fun borrowBook(book: Book) {
        viewModelScope.launch {
            val updatedBooks = _booksLiveData.value?.map {
                if (it.id == book.id) {
                    if (it.totalBookCount - it.borrowedCount == 0) {
                        _bookUnavailableChannel.send("Book '${it.title}' is not available")
                        it
                    } else {
                        it.copy(borrowedCount = it.borrowedCount + 1)
                    }
                } else it
            } ?: return@launch

            _booksLiveData.value = updatedBooks
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