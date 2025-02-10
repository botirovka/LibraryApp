package com.botirovka.libraryapp.bookEdit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Book
import com.example.domain.model.ChangeBookRequest
import com.example.domain.usecase.ChangeBookUseCase
import com.example.domain.usecase.GetBookByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditBookViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val changeBookUseCase: ChangeBookUseCase
): ViewModel() {

    private val _bookDetails = MutableLiveData<Book>()
    val bookDetails: LiveData<Book> = _bookDetails

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error


    private val _changed = MutableLiveData<Boolean>()
    val changed: LiveData<Boolean> = _changed



    public fun loadBookDetails(bookId: Int) {
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

    fun changeBook(changeBookRequest: ChangeBookRequest) {
        Log.d("mydebugChange", "changeBookVM: $changeBookRequest")
        if(changeBookUseCase.invoke(changeBookRequest)){
            _changed.value = true
        }
    }

}