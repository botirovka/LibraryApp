package com.botirovka.libraryapp.bookDetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.bookList.BookAdapter
import com.botirovka.libraryapp.bookList.BookListItem
import com.botirovka.libraryapp.databinding.FragmentBookDetailsBinding
import com.bumptech.glide.Glide
import com.example.domain.model.Book
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookDetailsFragment : Fragment() {
    private lateinit var binding: FragmentBookDetailsBinding
    private val args: BookDetailsFragmentArgs by navArgs()
    private val viewModel: BookDetailsViewModel by viewModels()

    private lateinit var booksRecyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        if (savedInstanceState == null) {
            viewModel.loadBookDetails(args.bookId)
        }
        booksRecyclerView = binding.booksRecyclerView


        bookAdapter =
            BookAdapter(
                ::onBorrowButtonClick,
                ::onItemViewClick,
                ::onFavoriteImageViewClick,
                binding.toolbar
            )
        bookAdapter.submitList(emptyList())
        booksRecyclerView.adapter = bookAdapter
        binding.reviewsButton.setOnClickListener {
            findNavController().navigate(
                BookDetailsFragmentDirections.actionBookDetailsFragmentToApiFragment()
            )
        }

        binding.editBookButton.setOnClickListener {
            findNavController().navigate(
                BookDetailsFragmentDirections.actionBookDetailsFragmentToEditBookFragment(
                    args.bookId
                )
            )
        }

    }


    private fun onLongItemClick(book: Book) {

    }

    private fun onBorrowButtonClick(book: Book) {
        viewModel.borrowBook(book)
    }

    private fun onFavoriteImageViewClick(book: Book) {
        Log.d("mydebugg", "onFavoriteImageViewClick: ")
        viewModel.changeBookFavoriteStatus(book)
    }

    private fun onItemViewClick(book: Book) {
        findNavController().navigate(
            BookDetailsFragmentDirections.actionBookDetailsFragmentSelf(book.id)
        )
    }


    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingImageProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.loadingDetailsProgressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.headerContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.detailsTextGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.errorTextView.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.errorTextView.text = errorMessage
                binding.errorTextView.visibility = View.VISIBLE
                binding.detailsTextGroup.visibility = View.GONE
                binding.headerContainer.visibility = View.GONE
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }

        viewModel.bookDetails.observe(viewLifecycleOwner) { book ->
            if (book != null) {
                binding.bookDetailsToolbar.title = book.title
                binding.bookTitleTextView.text = book.title
                binding.authorTextView.text = book.author
                binding.genreTextView.text = book.genre.toString()
                binding.DescriptionTextView.text = getString(R.string.very_long_test_string)
                if (book.image.isNotEmpty()) {
                    binding.bookImageView.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(book.image)
                        .placeholder(R.drawable.ic_book)
                        .into(binding.bookImageView)
                } else {
                    binding.bookImageView.visibility = View.VISIBLE
                }
                binding.availableTextView.text =
                    "${book.totalBookCount - book.borrowedCount}/${book.totalBookCount}"
                binding.detailsTextGroup.visibility = View.VISIBLE
                binding.headerContainer.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
        }

        viewModel.suggestedBooksLiveData.observe(viewLifecycleOwner) { books ->
            val bookListItems = books.map { BookListItem.BookItem(it) }
            bookAdapter.submitList(bookListItems)
            booksRecyclerView.visibility = View.VISIBLE

        }

        viewModel.loadingSuggestedBooks.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingSuggestedBooksProgressBar.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            booksRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }


}