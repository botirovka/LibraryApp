package com.botirovka.libraryapp.bookList

import com.example.domain.model.Author
import com.example.domain.model.Book

sealed class BookListItem {
    data class BookItem(val book: Book) : BookListItem()
    data class AuthorItem(val author: Author) : BookListItem()
}