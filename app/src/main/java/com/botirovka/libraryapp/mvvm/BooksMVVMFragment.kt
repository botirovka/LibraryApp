package com.botirovka.libraryapp.mvvm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.databinding.FragmentBookMVVMBinding
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.Genres
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BooksMVVMFragment : Fragment() {
    private lateinit var binding: FragmentBookMVVMBinding
    private val booksViewModel by viewModels<BooksViewModel>()
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loadMoreProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var createNewBookButton: Button
    private lateinit var fetchAllBookButton: Button
    private var isInitialTextChange = true
    private var isAllBookLoaded = false
    private var isMoreBookLoading: Boolean = false
    private var currentBooks: List<Book> = emptyList()
    private var query : String = ""
    private var isLoading: Boolean = false




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookMVVMBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        booksRecyclerView = binding.booksRecyclerView
        loadingProgressBar = binding.loadingProgressBar
        loadMoreProgressBar = binding.loadMoreProgressBar
        errorTextView = binding.errorTextView
        searchEditText = binding.searchEditText
        bookAdapter = BookAdapter(::onBorrowButtonClick, ::onFavoriteImageViewClick)
        booksRecyclerView.adapter = bookAdapter
        createNewBookButton = binding.createNewBookButton
        fetchAllBookButton = binding.fetchAllBookButton

        observeViewModel()
        setupInfiniteScroll()
        setupSearch()

        createNewBookButton.setOnClickListener {
            Log.d("mydebugMVVM", "end loadBooksParallel: ${currentBooks.size}")
            createNewBook()
        }

        fetchAllBookButton.setOnClickListener {
            Log.d("mydebugMVVM", "start fetch all books")
                booksViewModel.fetchBooks()

        }


    }

    private fun createNewBook() {
        val newBook = Library.createNewBook()
        booksViewModel.addNewBook(newBook)
        val layoutManager = booksRecyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        if (lastVisibleItemPosition >= currentBooks.size - 2 && isMoreBookLoading.not() && isLoading.not()){
            Log.d("mydebugPag", "loadBooks from createNewBook: $query")
                booksViewModel.loadMoreBooks()
        }
    }

    private fun setupInfiniteScroll() {
        booksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()


                if (lastVisibleItemPosition == currentBooks.size - 1 && isAllBookLoaded.not() && isMoreBookLoading.not()) {
                    Log.d("mydebugMVVM", " $isAllBookLoaded")
                    Log.d("mydebugMVVM", "LAST VISIBLE $lastVisibleItemPosition")
                    Log.d("mydebugMVVM", "Current last index: ${currentBooks.size - 1}")
                    Log.d("mydebugPag", "loadBooks from scroll: $query")
                    booksViewModel.loadMoreBooks()

                }
            }
        })
    }


    private fun onBorrowButtonClick(book: Book) {
        booksViewModel.borrowBook(book)
    }

    private fun onFavoriteImageViewClick(book: Book) {
        booksViewModel.changeBookFavoriteStatus(book)
    }

    private fun observeViewModel() {
        booksViewModel.booksLiveData.observe(viewLifecycleOwner) { books ->
            bookAdapter.submitList(books)
            currentBooks = books
            booksRecyclerView.visibility = View.VISIBLE

        }

        booksViewModel.loadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            this.isLoading = isLoading
            loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            booksRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
            errorTextView.visibility = View.GONE
        }

        booksViewModel.loadingMoreLiveData.observe(viewLifecycleOwner) { isLoading ->
            this.isMoreBookLoading = isLoading
            loadMoreProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            errorTextView.visibility = View.GONE
        }


        booksViewModel.errorLiveData.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                errorTextView.text = errorMessage
                errorTextView.visibility = View.VISIBLE
                booksRecyclerView.visibility = View.GONE
            } else {
                errorTextView.visibility = View.GONE
            }
        }

        lifecycleScope.launch {
            booksViewModel.bookUnavailableFlow.collectLatest { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launch {
            Log.d("mydebugMVVM", " $isAllBookLoaded before stateFlow")
            booksViewModel.isAllBookLoadedStateFlow.collectLatest {
                isAllBookLoaded = it
                Log.d("mydebugMVVM", " $isAllBookLoaded after stateFlow")
            }

        }
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isInitialTextChange) {
                    isInitialTextChange = false
                    return
                }
                query = s.toString().trim()
                Log.d("mydebugPag", "loadBooks from search: $query")
                booksViewModel.loadMoreBooks(query)
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }
}