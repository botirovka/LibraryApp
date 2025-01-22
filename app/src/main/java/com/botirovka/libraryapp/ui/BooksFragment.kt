package com.botirovka.libraryapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book
import com.botirovka.libraryapp.models.State
import com.botirovka.libraryapp.ui.adapters.BooksAdapter
import kotlinx.coroutines.launch

class BooksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var bookList: List<Book>
    private var isBorrowedFragment: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)
        recyclerView = view.findViewById(R.id.books_rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        isBorrowedFragment = arguments?.getBoolean("is_borrowed") ?: false

        loadBooks()

        return view
    }

    private fun groupBooks(){

    }

    private fun loadBooks() {
        lifecycleScope.launch {

            val booksState : State
            if(isBorrowedFragment ){
                booksState = Library.getAllBorrowedBooks()

            }
            else{
                booksState = Library.getAllBooks()
            }

            when (booksState) {
                is State.Data -> {
                    booksAdapter = BooksAdapter(booksState.data) {
                        book ->
                        if(book.borrowedCount > 0){
                            Library.returnBook(book.title)
                        }
                        else{
                            Library.borrowBook(book.title)
                        }
                        booksAdapter.notifyDataSetChanged();
                    }
                    recyclerView.adapter = booksAdapter
                }
                is State.Error -> {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }

                State.Loading -> {
                    Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

