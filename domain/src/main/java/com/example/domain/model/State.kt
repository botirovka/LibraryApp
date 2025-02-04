package com.example.domain.model

sealed class State {
    data object Loading : State()
    class Data(val data: List<Book>) : State()
    class Error(val message: String) : State()
}
