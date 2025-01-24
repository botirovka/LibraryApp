package com.botirovka.libraryapp.data

import android.util.Log
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.Extensions.Companion.groupByGenre
import com.botirovka.libraryapp.models.Extensions.Companion.sortedByTitleAvailableFirstAscending
import com.botirovka.libraryapp.models.Genres
import com.botirovka.libraryapp.models.State
import kotlinx.coroutines.Delay
import kotlinx.coroutines.delay
import java.time.LocalTime

object Library {
    private val books: MutableList<Book>
    val mockDelay: Long = 2000L

    init {
        books = mutableListOf(
            Book("The Hobbit", "J.R.R. Tolkien", Genres.FANTASY, "https://i.imgur.com/mJmmCUv.png"),
            Book(
                "The Girl with the Dragon Tattoo",
                "Stieg Larsson",
                Genres.THRILLER,
                "https://i.imgur.com/hIkr1pP.png",
                2,
                12
            ),
            Book(
                "Dune",
                "Frank Herbert",
                Genres.SCIENCE_FICTION,
                "https://i.imgur.com/Nn1UyoW.png",
                0,
                9
            ),
            Book(
                "Treasure Island",
                "Robert Louis Stevenson",
                Genres.ADVENTURE_FICTION,
                "https://imgur.com/tqvpAMm.png",
                0,
                5
            ),
            Book(
                "Pride and Prejudice",
                "Jane Austen",
                Genres.CLASSIC,
                "https://i.imgur.com/oh0bS0P.png",
                0,
                3
            ),
            Book(
                "Harry Potter and the Sorcerer's Stone",
                "J.K. Rowling",
                Genres.FANTASY,
                "https://i.imgur.com/bEe75Ri.png",
                5,
                10,
                true,
                System.currentTimeMillis()
            ),
            Book("Test Book", "Without Image", Genres.THRILLER)
        )
    }

    //Refactor all expensive functions
    suspend fun getAllBooks(): State {
        delay(mockDelay+2000)
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

    suspend fun getAllBooksByGenre(): Map<Genres, List<Book>> {
        return books.groupByGenre()
    }

    suspend fun getSortedBooksByAvailability(): List<Book> {
        return books.sortedByTitleAvailableFirstAscending()
    }

    suspend fun countBooksByGenre(): Map<Genres, Int> {
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

    suspend fun getBorrowedBooksSummaryByGenre(): Map<Genres, Int> {
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

    suspend fun getAvailableBooks(): List<Book>{
        delay(mockDelay+1000)
        return books.filter { it.isAvailable }
    }

    suspend fun getUniqueAuthors(): Set<String>{
        delay(mockDelay+100)
        return books.groupBy { it.author }.keys
    }


    fun addBook(book: Book): Int {
        books.add(book)
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

        if (bookByTitle != null && bookByTitle.totalBookCount > 0) {
            bookByTitle.borrowedCount++
            bookByTitle.totalBookCount--
            bookByTitle.lastBorrowedTime = System.currentTimeMillis()
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


}