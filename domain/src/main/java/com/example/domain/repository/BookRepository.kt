package com.example.domain.repository

import com.example.domain.model.Author
import com.example.domain.model.Book
import com.example.domain.model.ChangeBookRequest
import com.example.domain.model.Genres
import com.example.domain.model.Review
import com.example.domain.model.State

interface BookRepository {
    suspend fun getBooksPaginated(startIndex: Int, pageSize: Int, query: String): List<Book>
    suspend fun getBorrowedBooksPaginated(page: Int, pageSize: Int): List<Book>
    suspend fun getAllBooks(query: String): State
    suspend fun getAllBorrowedBooks(): State
    suspend fun getAllBooksByGenre(): Map<Genres, List<Book>>
    suspend fun getSortedBooksByAvailability(): List<Book>
    suspend fun countBooksByGenre(): Map<Genres, Int>
    suspend fun countBooksByAuthor(): Map<String, Int>
    suspend fun getMostPopularBooks(topCount: Int): List<Book>
    suspend fun getBorrowedBooksSummaryByGenre(): Map<Genres, Int>
    suspend fun getTrendingAuthor(): String?
    suspend fun getAvailableBooks(): List<Book>
    suspend fun getUniqueAuthors(): Set<String>
    suspend fun getBookById(bookId: Int): Book?
    suspend fun getReviewsByBookId(bookId: Int): List<Review>
    fun changeBook(changeBookRequest: ChangeBookRequest): Boolean
    suspend fun addBook(book: Book): Int
    suspend fun searchBooks(request: String): List<Book>
    suspend fun searchAuthors(request: String): List<Author>
    fun borrowBook(title: String): Boolean
    fun addBookToFavorite(id: Int): Boolean
    fun returnBook(title: String): Boolean
    fun createNewBook(): Book
}