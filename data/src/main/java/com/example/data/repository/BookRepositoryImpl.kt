package com.example.data.repository


import com.example.data.Library
import com.example.data.model.toAddBookRequest
import com.example.domain.model.Book
import com.example.domain.model.ChangeBookRequest
import com.example.domain.model.Genres
import com.example.domain.model.State
import com.example.domain.repository.BookRepository
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor() : BookRepository {
    override suspend fun getBooksPaginated(startIndex: Int, pageSize: Int, query: String): List<Book> {
        return Library.getBooksPaginated(startIndex, pageSize, query)
    }

    override suspend fun getBorrowedBooksPaginated(page: Int, pageSize: Int): List<Book> {
        return Library.getBorrowedBooksPaginated(page, pageSize)
    }

    override suspend fun getAllBooks(query: String): State {
        return Library.getAllBooks(query)
    }

    override suspend fun getAllBorrowedBooks(): State {
        return Library.getAllBorrowedBooks()
    }

    override suspend fun getAllBooksByGenre(): Map<Genres, List<Book>> {
        return Library.getAllBooksByGenre()
    }

    override suspend fun getSortedBooksByAvailability(): List<Book> {
        return Library.getSortedBooksByAvailability()
    }

    override suspend fun countBooksByGenre(): Map<Genres, Int> {
        return Library.countBooksByGenre()
    }

    override suspend fun countBooksByAuthor(): Map<String, Int> {
        return Library.countBooksByAuthor()
    }

    override suspend fun getMostPopularBooks(topCount: Int): List<Book> {
        return Library.getMostPopularBooks(topCount)
    }

    override suspend fun getBorrowedBooksSummaryByGenre(): Map<Genres, Int> {
        return Library.getBorrowedBooksSummaryByGenre()
    }

    override suspend fun getTrendingAuthor(): String? {
        return Library.getTrendingAuthor()
    }

    override suspend fun getAvailableBooks(): List<Book> {
        return Library.getAvailableBooks()
    }

    override suspend fun getUniqueAuthors(): Set<String> {
        return Library.getUniqueAuthors()
    }

    override suspend fun getBookById(bookId: Int): Book? {
        return Library.getBookById(bookId)
    }

    override fun changeBook(changeBookRequest: ChangeBookRequest): Boolean {
        return Library.changeBook(changeBookRequest)
    }

    override fun addBook(book: Book): Int {
        return Library.addBook(book.toAddBookRequest())
    }

    override suspend fun searchBooks(request: String): List<Book> {
        return Library.searchBooks(request)
    }

    override fun borrowBook(title: String): Boolean {
        return Library.borrowBook(title)
    }

    override fun addBookToFavorite(id: Int): Boolean {
        return Library.addBookToFavorite(id)
    }

    override fun returnBook(title: String): Boolean {
        return Library.returnBook(title)
    }

    override fun createNewBook(): Book {
        return Library.createNewBook()
    }
}