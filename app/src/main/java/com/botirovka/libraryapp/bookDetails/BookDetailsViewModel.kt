package com.botirovka.libraryapp.bookDetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Book
import com.example.domain.usecase.BorrowBookUseCase
import com.example.domain.usecase.GetBookByIdUseCase
import com.example.domain.usecase.SearchBooksUseCase
import com.example.domain.usecase.ToggleFavoriteBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val toggleFavoriteBookUseCase: ToggleFavoriteBookUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val borrowBookUseCase: BorrowBookUseCase
): ViewModel() {

    private val _bookDetails = MutableLiveData<Book>()
    val bookDetails: LiveData<Book> = _bookDetails

    private val _suggestedBooksLiveData = MutableLiveData<List<Book>>(emptyList())
    val suggestedBooksLiveData: LiveData<List<Book>> get() = _suggestedBooksLiveData

    private val _loadingSuggestedBooks = MutableLiveData<Boolean>()
    val loadingSuggestedBooks: LiveData<Boolean> = _loadingSuggestedBooks

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()





    fun loadBookDetails(bookId: Int) {
        _loading.value = true
        viewModelScope.launch {
            val book = bookId?.let { getBookByIdUseCase(it) }

            if(book != null){
                _bookDetails.value = book!!
                _loading.value = false
                loadSuggestedBooks(book)
            }
            else{
                _error.value = "Error: The book was not found"
            }

        }
    }

    fun loadSuggestedBooks(currentBook: Book) {
        _loadingSuggestedBooks.value = true
        viewModelScope.launch {
            val books = currentBook?.let { searchBooksUseCase(it.genre.name) }
            Log.d("mydebugSuggested", "loadSuggestedBooks: $books")
            if(books != null){
                val filteredBooks = books.filter { it.id != currentBook.id }
                _suggestedBooksLiveData.value = filteredBooks
            }
            else{
                _suggestedBooksLiveData.value = emptyList()
            }
            _loadingSuggestedBooks.value = false
        }
    }

    fun borrowBook(book: Book) {
        viewModelScope.launch {
            val isBorrowed = borrowBookUseCase(book.title)
            if (isBorrowed) {
                _suggestedBooksLiveData.value = _suggestedBooksLiveData.value

            } else {
                _bookUnavailableChannel.send("Book '${book.title}' is not available")
            }
        }
    }


    fun changeBookFavoriteStatus(book: Book) {
        viewModelScope.launch {
            val isChanged = toggleFavoriteBookUseCase(book.id)
            if (isChanged) {
                _suggestedBooksLiveData.value = _suggestedBooksLiveData.value

            } else {
                _error.value = "Unexpected error"
            }
        }
    }
}