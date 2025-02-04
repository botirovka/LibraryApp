package com.example.data

import android.util.Log
import com.example.data.repository.BookRepositoryImpl
import com.example.domain.DIReplacer

object Repo {

    init {
        Log.d("mydebugCA", "repository init")
        DIReplacer.bookRepository = BookRepositoryImpl()
    }

    fun doSomeTest() {
        Log.d("mydebugCA", "repo test")
    }

}