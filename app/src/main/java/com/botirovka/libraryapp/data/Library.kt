package com.botirovka.libraryapp.data

import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.Extensions.Companion.groupByGenre
import com.botirovka.libraryapp.models.Extensions.Companion.sortedByTitleAvailableFirstAscending
import com.botirovka.libraryapp.models.Genres
import com.botirovka.libraryapp.models.State
import kotlinx.coroutines.delay

object Library {
    private val books: MutableList<Book>

    init {
        books = mutableListOf(
            Book("The Hobbit", "J.R.R. Tolkien", Genres.FANTASY, "https://i.imgur.com/mJmmCUv.png"),
            Book(
                "The Girl with the Dragon Tattoo",
                "Stieg Larsson",
                Genres.THRILLER,
                "https://i.imgur.com/hIkr1pP.png"
            ),
            Book(
                "Dune",
                "Frank Herbert",
                Genres.SCIENCE_FICTION,
                "https://i.imgur.com/Nn1UyoW.png"
            ),
            Book(
                "Treasure Island",
                "Robert Louis Stevenson",
                Genres.ADVENTURE_FICTION,
                "https://imgur.com/tqvpAMm.png"
            ),
            Book(
                "Pride and Prejudice",
                "Jane Austen",
                Genres.CLASSIC,
                "https://i.imgur.com/oh0bS0P.png"
            ),
            Book(
                "Harry Potter and the Sorcerer's Stone",
                "J.K. Rowling",
                Genres.FANTASY,
                "https://i.imgur.com/bEe75Ri.png",
                5
            ),
            Book("Test Book", "Without Image", Genres.THRILLER)
        )
    }

    suspend fun getAllBooks(): State {
        delay(2000)
        return if (books.isNotEmpty()) {
            State.Data(books.toList())
        } else {
            State.Error("No books found")
        }
    }

    suspend fun getAllBorrowedBooks(): State {
        delay(2000)
        return if (books.isNotEmpty()) {
            State.Data(books.filter { it.borrowedCount > 0 }.toList())
        } else {
            State.Error("No books found")
        }
    }
    //Task 1.2
    fun getAllBooksByGenre(): Map<Genres, List<Book>> {
        return books.groupByGenre()
    }

    fun getSortedBooksByAvailability(): List<Book> {
        return books.sortedByTitleAvailableFirstAscending()
    }

    fun countBooksByGenre(): Map<Genres, Int> {
        return books.groupBy { it.genre }.mapValues { it.value.size }
    }

    fun countBooksByAuthor(): Map<String, Int> {
        return books.groupBy { it.author }.mapValues { it.value.size }
    }

    fun getMostPopularBooks(topCount: Int): List<Book> {
        return books
            .filter { it.borrowedCount > 0 }
            .sortedByDescending { it.borrowedCount }
            .take(topCount)
    }

    fun getBorrowedBooksSummaryByGenre(): Map<Genres, Int> {
        return books
            .filter { it.borrowedCount > 0 }
            .groupBy { it.genre }
            .mapValues { entry ->
                entry.value.sumOf { it.borrowedCount }
            }
    }

    fun getTrendingAuthor(): String? {
        val currentTime = System.currentTimeMillis()

        return books
            .filter { it.lastBorrowedTime != null && currentTime - it.lastBorrowedTime!! <= 5 * 60 * 1000 }
            .groupBy { it.author }
            .maxByOrNull { it.value.size }
            ?.key
    }

    fun getAvailableBooks(): List<Book>{
        return books.filter { it.isAvailable }
    }

    fun getUniqueAuthors(): Set<String>{
        return books.groupBy { it.author }.keys
    }


    fun addBook(book: Book): Int {
        books.add(book)
        return books.lastIndex
    }

    fun searchBooks(request: String): List<Book> {
        val lowerCaseRequest = request.lowercase()
        return books.filter {
            it.title.lowercase().contains(lowerCaseRequest) ||
                    it.genre.name.lowercase().contains(lowerCaseRequest)
        }
    }

    fun borrowBook(title: String): Boolean {
        val bookByTitle = books.find { it.title == title }
        if (bookByTitle != null) {
            bookByTitle.borrowedCount++
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