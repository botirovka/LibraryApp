package com.botirovka.libraryapp.mvp

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
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.databinding.FragmentBooksMVPBinding
import com.botirovka.libraryapp.models.Book
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.mvvm.BookAdapter

class BooksMVPFragment : Fragment(), ShowBookView {
    private lateinit var binding: FragmentBooksMVPBinding
    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var loadingMoreProgressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var createNewBookButton: Button
    private lateinit var fetchAllBookButton: Button
    private var isInitialTextChange = true
    private var currentBooks: List<Book> = emptyList()
    private var query : String = ""

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
        loadingMoreProgressBar = binding.loadMoreProgressBar
        errorTextView = binding.errorTextView
        searchEditText = binding.searchEditText
        bookAdapter = BookAdapter(::onBorrowButtonClickMVP, ::onFavoriteImageViewClickMVP)
        booksRecyclerView.adapter = bookAdapter
        createNewBookButton = binding.createNewBookButton
        fetchAllBookButton = binding.fetchAllBookButton
        Log.d("mydebugMVP", "F $view")



        Presenter.attachView(this)
        if (savedInstanceState == null) {
            Presenter.reset(this)
        }
        setupSearch()

        setupInfiniteScroll()


        observeBookUnavailableFlow()



        createNewBookButton.setOnClickListener {
            Log.d("mydebugMVVM", "end loadBooksParallel: ${currentBooks.size}")
            createNewBook()
        }

        fetchAllBookButton.setOnClickListener {
            if(Presenter.isAllBookLoaded){
                Toast.makeText(context,"All books already loaded",Toast.LENGTH_SHORT).show()
            }
            else{
                Log.d("mydebugMVVM", "start fetch all books")
                Presenter.fetchBooks()
            }
        }
    }



    private fun createNewBook() {
        val newBook = Library.createNewBook()
        Presenter.addNewBook(newBook)
        val layoutManager = booksRecyclerView.layoutManager as LinearLayoutManager
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        if (lastVisibleItemPosition >= currentBooks.size - 2 && Presenter.isMoreBookLoading.not() && Presenter.isLoading.not()){
            Log.d("mydebugPag", "loadBooks from createNewBook: $query")
            Presenter.loadMoreBooks()
        }
    }

    private fun setupInfiniteScroll() {
        booksRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                Log.d("mydebugMVP", "isAllBookLoaded: ${Presenter.isAllBookLoaded}")
                if (lastVisibleItemPosition == currentBooks.size - 1
                    && Presenter.isAllBookLoaded.not()
                    && Presenter.isMoreBookLoading.not()) {
                    Presenter.loadMoreBooks()
                }
            }
        })
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
                Log.d("mydebugMVP", "setupSearch: $query")
                Presenter.loadMoreBooks(query)
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

    private fun onFavoriteImageViewClickMVP(book: Book) {
        Presenter.changeBookFavoriteStatus(book)
    }

    private fun onReturnButtonClickMVP(book: Book) {

    }

    override fun showBooks(books: List<Book>) {
        Log.d("mydebugMVP", "showBOok Fragment")
        bookAdapter.submitList(books)
        currentBooks = books
        booksRecyclerView.visibility = View.VISIBLE
        loadingMoreProgressBar.visibility = View.GONE
        loadingMoreProgressBar.visibility = View.GONE
        errorTextView.visibility = View.GONE
    }

    override fun showError(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        booksRecyclerView.visibility = View.GONE
    }

    override fun showLoading(isLoading: Boolean) {
        Log.d("mydebugMVP", "showLoading: $isLoading ")
        loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        loadingMoreProgressBar.visibility = if (isLoading) View.GONE else View.VISIBLE
        booksRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        errorTextView.visibility = View.GONE
    }

    override fun showMoreLoading(isMoreLoading: Boolean) {
        Log.d("mydebugMVP", "showMoreLoading: $isMoreLoading ")
        loadingMoreProgressBar.visibility = if (isMoreLoading) View.VISIBLE else View.GONE
        loadingProgressBar.visibility = if (isMoreLoading) View.GONE else View.VISIBLE
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


    override fun getCurrentBooks(): List<Book> {
        return currentBooks
    }
}