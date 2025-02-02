package com.botirovka.libraryapp.mvvm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Import для viewModels()
import androidx.recyclerview.widget.RecyclerView

import androidx.lifecycle.lifecycleScope
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
    private lateinit var errorTextView: TextView
    private lateinit var searchEditText: EditText
    private var isInitialTextChange = true

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
        errorTextView = binding.errorTextView
        searchEditText = binding.searchEditText

        bookAdapter = BookAdapter(::onBorrowButtonClickMVI)

        booksRecyclerView.adapter = bookAdapter

        setupSearch()
        observeState()
    }

    private fun onBorrowButtonClickMVI(book: Book) {
        viewModel.processIntent(BooksIntent.BorrowBook(book))
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
                viewModel.processIntent(BooksIntent.SearchBooks(BooksIntent.SearchBooks(query).query))
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is BooksState.Loading -> showLoading()
                    is BooksState.Success -> showBooks(state.books)
                    is BooksState.Error -> showError(state.message)
                    is BooksState.BookUnavailable -> showBookUnavailableMessage(state.message)
                    is BooksState.Idle -> hideMessage()
                    else -> {}
                }
            }
        }
    }

    private fun showBooks(books: List<Book>) {
        bookAdapter.submitList(books)
        booksRecyclerView.visibility = View.VISIBLE
        loadingProgressBar.visibility = View.GONE
        errorTextView.visibility = View.GONE
    }

    private fun showError(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        booksRecyclerView.visibility = View.GONE
        loadingProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        booksRecyclerView.visibility = View.GONE
        errorTextView.visibility = View.GONE
    }

    private fun showBookUnavailableMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        viewModel.processIntent(BooksIntent.ClearBookUnavailableMessage)
    }

    private fun hideMessage() {
        errorTextView.visibility = View.GONE
    }
}