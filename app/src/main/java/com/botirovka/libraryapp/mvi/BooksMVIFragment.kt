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
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.botirovka.libraryapp.databinding.FragmentBooksMVIBinding
import com.botirovka.libraryapp.models.Book
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BooksMVIFragment : Fragment() {
    private lateinit var binding: FragmentBooksMVIBinding
    private val viewModel: BooksViewModelMVI by viewModels()
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
    private var isLoading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBooksMVIBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        booksRecyclerView = binding.booksRecyclerView
        loadingProgressBar = binding.loadingProgressBar
        loadMoreProgressBar = binding.loadMoreProgressBar
        errorTextView = binding.errorTextView
        searchEditText = binding.searchEditText
        createNewBookButton = binding.createNewBookButton
        fetchAllBookButton = binding.fetchAllBookButton

        bookAdapter = BookAdapter(::onBorrowButtonClickMVI, ::onFavoriteImageViewClickMVI)
        booksRecyclerView.adapter = bookAdapter

        if (savedInstanceState != null){
           viewModel.processIntent(BooksIntent.LoadCachedBooks)
        }
        observeState()
        setupSearch()
        setupInfiniteScroll()


        createNewBookButton.setOnClickListener{
            createNewBook()
        }

        fetchAllBookButton.setOnClickListener{
            viewModel.processIntent(BooksIntent.FetchAllBooks)
        }
    }

    private fun createNewBook() {
        viewModel.processIntent(BooksIntent.CreateNewBook)
        val layoutManager = booksRecyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        if (lastVisibleItemPosition >= currentBooks.size - 2 && isMoreBookLoading.not() && isLoading.not()){
            viewModel.processIntent(BooksIntent.LoadMoreBooks())
            Log.d("mydebugMVI", "loadMore from: createNewBOok")
        }
        else{
            Log.d("mydebugMVI", "loadMore from: createNewBOok failed")
            Log.d("mydebugMVI", "isMoreBookLoading $isMoreBookLoading isLoading  $isLoading")
        }
    }

    private fun setupInfiniteScroll() {
        booksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItemPosition == currentBooks.size - 1 && !isAllBookLoaded && !isMoreBookLoading) {
                    Log.d("mydebugMVI", "loadMore from: scroll")
                    viewModel.processIntent(BooksIntent.LoadMoreBooks())
                }
                else{
                    Log.d("mydebugMVI", "loadMore failed from:  scroll")
                }
            }
        })
    }

    private fun onBorrowButtonClickMVI(book: Book) {
        viewModel.processIntent(BooksIntent.BorrowBook(book))
    }

    private fun onFavoriteImageViewClickMVI(book: Book) {
        viewModel.processIntent(BooksIntent.ChangeFavoriteStatus(book))
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isInitialTextChange) {
                    isInitialTextChange = false
                    return
                }
                val query = s.toString().trim()
                Log.d("mydebugMVI", "loadMore from: search")
                viewModel.processIntent(BooksIntent.LoadMoreBooks(query))
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is BooksState.Loading -> showLoading()
                    is BooksState.LoadingMore -> showLoadingMore()
                    is BooksState.Success -> showBooks(state.books).also { Log.d("mydebugMVI", "show") }
                    is BooksState.Error -> showError(state.message)
                    is BooksState.BookUnavailable -> showBookUnavailableMessage(state.message)
                    is BooksState.AllBooksLoaded -> isAllBookLoaded = state.bool
                    is BooksState.Idle -> hideMessage()
                    else -> {}
                }
            }
        }
        lifecycleScope.launch {
            viewModel.bookUnavailableFlow.collectLatest { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showBooks(books: List<Book>) {
        bookAdapter.submitList(books)
        currentBooks = books
        booksRecyclerView.visibility = View.VISIBLE
        loadingProgressBar.visibility = View.GONE
        loadMoreProgressBar.visibility = View.GONE
        errorTextView.visibility = View.GONE
        isLoading = false
        isMoreBookLoading = false
    }

    private fun showError(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        booksRecyclerView.visibility = View.GONE
        loadingProgressBar.visibility = View.GONE
        loadMoreProgressBar.visibility = View.GONE
        isLoading = false
        isMoreBookLoading = false
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        booksRecyclerView.visibility = View.GONE
        errorTextView.visibility = View.GONE
        loadMoreProgressBar.visibility = View.GONE
        isLoading = true
        isMoreBookLoading = false
    }

    private fun showLoadingMore() {
        loadMoreProgressBar.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        isLoading = false
        isMoreBookLoading = true
    }


    private fun showBookUnavailableMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        viewModel.processIntent(BooksIntent.ClearBookUnavailableMessage)
    }

    private fun hideMessage() {
        errorTextView.visibility = View.GONE
    }
}