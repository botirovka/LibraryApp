package com.example.data

import android.util.Log
import com.example.data.model.AddBookRequest
import com.example.data.model.toBook
import com.example.domain.extensions.Extensions.Companion.groupByGenre
import com.example.domain.extensions.Extensions.Companion.sortedByTitleAvailableFirstAscending
import com.example.domain.model.Book
import com.example.domain.model.ChangeBookRequest
import com.example.domain.model.Genres
import com.example.domain.model.State

import kotlinx.coroutines.delay
import kotlin.enums.enumEntries

object Library {
    private val books: MutableList<Book>
    private const val mockDelay: Long = 1000L


    init {
        books = mutableListOf(
            Book(
                0,
                "Harry Potter and the Sorcerer's Stone",
                "J.K. Rowling",
                com.example.domain.model.Genres.FANTASY,
                "https://i.imgur.com/bEe75Ri.png",
                5,
                10,
                true,
                System.currentTimeMillis(),
                true
            ),
            Book(
                0,
                "The Hobbit",
                "J.R.R. Tolkien",
                com.example.domain.model.Genres.FANTASY,
                "https://i.imgur.com/mJmmCUv.png"
            ),
            Book(
                0,
                "The Girl with the Dragon Tattoo",
                "Stieg Larsson",
                com.example.domain.model.Genres.THRILLER,
                "https://i.imgur.com/hIkr1pP.png",
                2,
                12
            ),
            Book(
                0,
                "Dune",
                "Frank Herbert",
                com.example.domain.model.Genres.SCIENCE_FICTION,
                "https://i.imgur.com/Nn1UyoW.png",
                0,
                9
            ),
            Book(
                0,
                "Treasure Island",
                "Robert Louis Stevenson",
                com.example.domain.model.Genres.ADVENTURE_FICTION,
                "https://imgur.com/tqvpAMm.png",
                0,
                5
            ),
            Book(
                0,
                "Pride and Prejudice",
                "Jane Austen",
                com.example.domain.model.Genres.CLASSIC,
                "https://i.imgur.com/oh0bS0P.png",
                0,
                3
            ),
            Book(
                0,
                "Harry Potter and The Order of the Fenix",
                "J.K. Rowling",
                com.example.domain.model.Genres.FANTASY,
                "https://imgur.com/izIUWtX.png",
                5,
                10,
                true,
                System.currentTimeMillis()
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            ),
            Book(
                0,
                "Test Book",
                "Without Image",
                com.example.domain.model.Genres.THRILLER
            )
        )
        books.forEachIndexed { index, book -> book.id = index }
    }

    fun createNewBook() : Book {
        return Book(
            id = books.size,
            title = "New Book Title ${books.size}",
            author = "New Book Author",
            genre = com.example.domain.model.Genres.FANTASY,
            totalBookCount = 10,
            borrowedCount = 0
        )
    }

    //TASK 1.5
    suspend fun getBooksPaginated(startIndex: Int, pageSize: Int, query: String = ""): List<Book> {
        delay(mockDelay)
        val booksByQuery = searchBooks(query)
        val endIndex = minOf(startIndex + pageSize, booksByQuery.size)

        return if (startIndex < booksByQuery.size) {
            booksByQuery.slice(startIndex until endIndex)
        } else {
            emptyList()
        }
    }

    suspend fun getBorrowedBooksPaginated(page: Int, pageSize: Int): List<Book> {
        delay(mockDelay)
        val borrowedBooks = books.filter { it.borrowedCount > 0 }
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, borrowedBooks.size)

        return if (startIndex < borrowedBooks.size) {
            borrowedBooks.slice(startIndex until endIndex)
        } else {
            emptyList()
        }
    }

    //Refactor all expensive functions
    suspend fun getAllBooks(query: String = ""): State {
        delay(mockDelay + 2000)
        if(query.isNotEmpty()){
            val booksByQuery = searchBooks(query)
            return if (booksByQuery.isNotEmpty()){
                State.Data(booksByQuery)
            }
            else {
                State.Error("No books found")
            }
        }
        return if (books.isNotEmpty()) {
            State.Data(books.toList())
        } else {
            State.Error("No books found")
        }
    }

    suspend fun getAllBorrowedBooks(): State {
        delay(mockDelay)
        return if (books.isNotEmpty()) {
            State.Data(books.filter { it.borrowedCount > 0 }.toList())
        } else {
            State.Error("No books found")
        }
    }

    suspend fun getAllBooksByGenre(): Map<com.example.domain.model.Genres, List<Book>> {
        return books.groupByGenre()
    }

    suspend fun getSortedBooksByAvailability(): List<Book> {
        return books.sortedByTitleAvailableFirstAscending()
    }

    suspend fun countBooksByGenre(): Map<com.example.domain.model.Genres, Int> {
        return books.groupBy { it.genre }.mapValues { it.value.size }
    }

    suspend fun countBooksByAuthor(): Map<String, Int> {
        return books.groupBy { it.author }.mapValues { it.value.size }
    }

    suspend fun getMostPopularBooks(topCount: Int): List<Book> {
        return books
            .filter { it.borrowedCount > 0 }
            .sortedByDescending { it.borrowedCount }
            .take(topCount)
    }

    suspend fun getBorrowedBooksSummaryByGenre(): Map<com.example.domain.model.Genres, Int> {
        delay(mockDelay)
        return books
            .filter { it.borrowedCount > 0 }
            .groupBy { it.genre }
            .mapValues { entry ->
                entry.value.sumOf { it.borrowedCount }
            }
    }

    suspend fun getTrendingAuthor(): String? {
        val currentTime = System.currentTimeMillis()
        delay(mockDelay)
        return books
            .filter { it.lastBorrowedTime != null && currentTime - it.lastBorrowedTime!! <= 5 * 60 * 1000 }
            .groupBy { it.author }
            .maxByOrNull { it.value.size }
            ?.key
    }

    suspend fun getAvailableBooks(): List<Book> {
        delay(mockDelay + 1000)
        return books.filter { it.isAvailable }
    }

    suspend fun getUniqueAuthors(): Set<String> {
        delay(mockDelay + 100)
        return books.groupBy { it.author }.keys
    }


    fun addBook(addBookRequest: AddBookRequest): Int {
        books.add(
            addBookRequest.toBook()
        )
        return books.lastIndex
    }

    suspend fun searchBooks(request: String): List<Book> {
        delay(mockDelay)
        val lowerCaseRequest = request.lowercase()
        return books.filter {
            it.title.lowercase().contains(lowerCaseRequest) ||
                    it.genre.name.lowercase().contains(lowerCaseRequest)
        }
    }

    fun borrowBook(title: String): Boolean {
        Log.d("mydebug", "borrowBook: ")
        val bookByTitle = books.find { it.title == title }

        if (bookByTitle != null && bookByTitle.totalBookCount - bookByTitle.borrowedCount > 0) {
            bookByTitle.borrowedCount++
            bookByTitle.lastBorrowedTime = System.currentTimeMillis()
            return true
        }
        return false
    }

    fun addBookToFavorite(id: Int): Boolean {
        Log.d("mydebug", "borrowBook: ")
        val bookByTitle = books.find { it.id == id }

        if (bookByTitle != null ) {
            bookByTitle.isFavorite = bookByTitle.isFavorite.not()
            return true
        }
        return false
    }


    fun returnBook(title: String): Boolean {
        val bookByTitle = books.find { it.title == title && it.borrowedCount > 0 }

        if (bookByTitle != null) {
            bookByTitle.borrowedCount--
            return true
        }
        return false
    }

    suspend fun getBookById(bookId: Int): Book? {
        delay(mockDelay)
        return books.find { it.id == bookId }
    }

    fun changeBook(changeBookRequest: ChangeBookRequest) : Boolean {
        Log.d("mydebugChange", "changeBook: $changeBookRequest")
        books.find { it.id == changeBookRequest.id }?.apply {
            changeBookRequest.title?.takeIf { it.isNotBlank() }?.let { title = it }
            changeBookRequest.author?.takeIf { it.isNotBlank() }?.let { author = it }
            changeBookRequest.genre?.takeIf { it.isNotBlank() }?.let {
                val genreParsed: Genres = enumValueOf(it)
                genre = genreParsed

            }
            isFavorite = changeBookRequest.isFavorite

        }
        return true
    }


}