package com.botirovka.libraryapp.mvp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.databinding.FragmentBooksMVPBinding
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.mvp.ShowBookView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.botirovka.libraryapp.mvvm.BookAdapter

class BooksMVPFragment : Fragment(), ShowBookView {
    private lateinit var binding: FragmentBooksMVPBinding
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var searchEditText: EditText
    private var currentBooks: List<Book> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBooksMVPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        booksRecyclerView = binding.booksRecyclerView
        loadingProgressBar = binding.loadingProgressBar
        errorTextView = binding.errorTextView
        searchEditText = binding.searchEditText
        bookAdapter = BookAdapter(::onBorrowButtonClickMVP)
        booksRecyclerView.adapter = bookAdapter
        Log.d("mydebug", "onViewCreated: ")
        Log.d("mydebug", (savedInstanceState != null).toString())

        Presenter.attachView(this)
        if (savedInstanceState == null) {
            Presenter.reset()
            Presenter.fetchBooks()

        }
        setupSearch()
        observeBookUnavailableFlow()


    }

    override fun onDestroyView() {
        Log.d("mydebug", "onDestroyView: ")
        super.onDestroyView()
        Presenter.detachView()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                Log.d("mydebug", "setupSearch: start")
                Presenter.searchBooks(query)
            }

            override fun afterTextChanged(editable: Editable?) {}
        })
    }

    private fun observeBookUnavailableFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            Presenter.bookUnavailableFlow.collectLatest { message ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onBorrowButtonClickMVP(book: Book) {
        Presenter.borrowBook(book)
    }

    private fun onReturnButtonClickMVP(book: Book) {

    }

    override fun showBooks(books: List<Book>) {
        bookAdapter.submitList(books)
        currentBooks = books
        booksRecyclerView.visibility = View.VISIBLE
    }

    override fun showError(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        booksRecyclerView.visibility = View.GONE
    }

    override fun showLoading(isLoading: Boolean) {
        loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        booksRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        errorTextView.visibility = View.GONE
    }

    override fun showBookUnavailableMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onBorrowButtonClick(book: Book) {
        TODO("Not yet implemented")
    }

    override fun onReturnButtonClick(book: Book) {
        TODO("Not yet implemented")
    }


    fun getCurrentBooks(): List<Book> {
        return currentBooks
    }
}