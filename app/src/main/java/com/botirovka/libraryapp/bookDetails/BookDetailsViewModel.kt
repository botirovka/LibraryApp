package com.botirovka.libraryapp.bookDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Book
import com.example.domain.usecase.GetBookByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class BookDetailsViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _bookDetails = MutableLiveData<Book>()
    val bookDetails: LiveData<Book> = _bookDetails

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val bookId: Int?
    init {
        bookId = savedStateHandle.get<Int>("bookId")
        loadBookDetails()
    }

    private fun loadBookDetails() {
        _loading.value = true
        viewModelScope.launch {
            val book = bookId?.let { getBookByIdUseCase(it) }
            if(book != null){
                _bookDetails.value = book!!
                _loading.value = false
            }
            else{
                _error.value = "Error: The book was not found"
            }

        }
    }
}