package com.botirovka.libraryapp.reviews

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Review
import com.example.domain.usecase.GetReviewsByBookIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val getReviewsByBookIdUseCase: GetReviewsByBookIdUseCase,
): ViewModel() {

    private val _reviewsLiveData = MutableLiveData<List<Review>>()
    val reviewsLiveData: LiveData<List<Review>> = _reviewsLiveData

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadReviewsByBookId(bookId: Int) {
        Log.d("mydebugReview", "loadReviewsByBookId: $bookId")
        _loading.value = true
        viewModelScope.launch {
            val reviews = getReviewsByBookIdUseCase(bookId)
            Log.d("mydebugReview", "loadReviewsByBookId: $reviews")
            if(reviews.isNotEmpty()){
                _reviewsLiveData.value = reviews
                _loading.value = false
            }
            else{
                _error.value = "This book doesn't have any reviews yet"
            }

        }
    }
}