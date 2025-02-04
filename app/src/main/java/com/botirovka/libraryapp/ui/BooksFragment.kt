package com.botirovka.libraryapp.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.example.domain.model.Book
import com.botirovka.libraryapp.ui.adapters.BooksAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class BooksFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var booksAdapter: BooksAdapter
    private var isBorrowedFragment: Boolean = false
    private var isAllBookLoaded: Boolean = false
    private var isLoading: Boolean = false
    private var books = mutableListOf<Book>()
    private var currentPage = 0
    private val pageSize = 4

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)
        recyclerView = view.findViewById(R.id.books_rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        isBorrowedFragment = arguments?.getBoolean("is_borrowed") ?: false
        setupInfiniteScroll()
        loadInitialBooks()

        return view
    }

    private fun setupInfiniteScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition == books.size - 1 && isAllBookLoaded.not()) {
                    loadMoreBooks(4)
                }
            }
        })
    }

    private fun loadInitialBooks(pagesToLoad: Int = 2) {
        lifecycleScope.launch {
            Log.d("mydebug", "start loadInitialBooks: "  + currentPage)
            val startPage = currentPage
            currentPage += pagesToLoad
            loadBooksParallel(startPage until startPage + pagesToLoad)
        }
    }

    private fun loadMoreBooks(pagesToLoad: Int = 2) {
        if (isLoading) return

        isLoading = true
        val startPage = currentPage
        currentPage += pagesToLoad

        lifecycleScope.launch {
            try {
                Log.d("mydebug", "start load more Books: $currentPage")
                loadBooksParallel(startPage until startPage + pagesToLoad)
            } finally {
                isLoading = false
            }
        }
    }


    private suspend fun loadBooksParallel(pageRange: IntRange) {
        try {
            Log.d("mydebug", "start loadBooksParallel: $currentPage")
            val booksResult = withContext(Dispatchers.IO) {
                val deferredPages = pageRange.map { page ->
                    async {
                        fetchBooksForPage(page)
                    }
                }

                deferredPages.awaitAll().flatten().also {
                    isAllBookLoaded = it.isEmpty()
                    Log.d("mydebug", "end loadBooksParallel: $currentPage")
                }
            }

            withContext(Dispatchers.Main) {
                updateBooksList(booksResult)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Error loading books: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun fetchBooksForPage(page: Int): List<Book> {
        val randomDelay = Random.nextLong(500, 2000)
        delay(randomDelay)

        return if (isBorrowedFragment) {
            com.example.data.Library.getBorrowedBooksPaginated(page, pageSize)
        } else {
            com.example.data.Library.getBooksPaginated(page, pageSize)
        }
    }

    private fun updateBooksList(newBooks: List<Book>) {
        val startPosition = books.size
        books.addAll(newBooks)

        if (startPosition == 0) {
            booksAdapter = BooksAdapter(books) { book ->
                handleBookBorrow(book)
            }
            recyclerView.adapter = booksAdapter
        } else {
            booksAdapter.notifyItemRangeInserted(startPosition, newBooks.size)
        }
    }

    private fun handleBookBorrow(book: Book) {
        if (book.totalBookCount > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = com.example.data.Library.borrowBook(book.title)
                withContext(Dispatchers.Main) {
                    if (result) {
                        booksAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "Failed to borrow the book", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Sorry, this book is not available", Toast.LENGTH_SHORT).show()
        }
    }
}