package com.botirovka.libraryapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.botirovka.libraryapp.databinding.ActivityMainBinding
import com.example.domain.extensions.Extensions.Companion.printPretty
import com.botirovka.libraryapp.mvvm.BooksMVVMFragment
import com.example.data.Repo
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI(savedInstanceState)
        doOnBackground()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun doOnBackground(){
        Repo.doSomeTest()
        Log.d("coroutines", "doOnBackground")
        //testAllCoroutinesAtTheSameTime()
    }

    //start and end simultaneously, work asynchronous

    private fun testAllCoroutinesAtTheSameTime() {
        CoroutineScope(Dispatchers.IO).launch {

                val getAllBooksDeferred = async {
                    Log.d("coroutines", "get all books started")
                    com.example.data.Library.getAllBooks().also {
                        Log.d("coroutines", "get all books ended")
                    }

                }
                val getAllBooksByGenreDeferred = async {
                    Log.d("coroutines", "get all books By genre started")
                    com.example.data.Library.getAllBooksByGenre().also {
                        Log.d("coroutines", "get all books By genre ended")
                    }
                }
                val getAvailableBooksDeferred = async {
                    Log.d("coroutines", "get available books started")
                    com.example.data.Library.getAvailableBooks().also {
                        Log.d("coroutines", "get available books ended")
                    }
                }
                val getUniqueAuthorsDeferred = async {
                    Log.d("coroutines", "get unique authors started")
                    com.example.data.Library.getUniqueAuthors()
                    Log.d("coroutines", "get unique authors ended")
                }
                val getMostPopularBooksDeferred = async {
                    Log.d("coroutines", "get most popular books started")
                    com.example.data.Library.getMostPopularBooks(3).also {
                        Log.d("coroutines", "get most popular books ended")
                    } }
                val getBorrowedBooksSummaryDeferred = async {
                    Log.d("coroutines", "get BorrowedBooksSummary started")
                    com.example.data.Library.getBorrowedBooksSummaryByGenre().also {
                        Log.d("coroutines", "get BorrowedBooksSummary ended")
                    } }
                val getTrendingAuthorDeferred = async {
                    Log.d("coroutines", "get Trending Author started")
                    com.example.data.Library.getTrendingAuthor().also {
                        Log.d("coroutines", "get Trending Author ended")
                    } }



                val allBooks = getAllBooksDeferred.await()
                val booksByGenre = getAllBooksByGenreDeferred.await()
                val availableBooks = getAvailableBooksDeferred.await()
                val uniqueAuthors = getUniqueAuthorsDeferred.await()
                val mostPopularBooks = getMostPopularBooksDeferred.await()
                val borrowedBooksSummary = getBorrowedBooksSummaryDeferred.await()
                val trendingAuthor = getTrendingAuthorDeferred.await()

                Log.d("results", "All books: $allBooks")
                Log.d("results", "Books by genre: $booksByGenre")
                Log.d("results", "Available books: ${availableBooks.printPretty()}")
                Log.d("results", "Unique authors: $uniqueAuthors")
                Log.d("results", "Most popular books: ${mostPopularBooks.printPretty()}")
                Log.d("results", "Borrowed books summary: $borrowedBooksSummary")
                Log.d("results", "Trending author: $trendingAuthor")


        }
    }

    private fun setupUI(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            replaceFragment(BooksMVVMFragment())
        }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNav = binding.bottomNav
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_book -> replaceFragment(BooksMVVMFragment())
                R.id.books -> replaceFragment(BooksMVVMFragment())
                R.id.borrowed_books -> replaceFragment(BooksMVVMFragment())
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment , isBorrowed: Boolean = false): Boolean {
        val bundle = Bundle()
        bundle.putBoolean("is_borrowed", isBorrowed)
        fragment.arguments = bundle

        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

}