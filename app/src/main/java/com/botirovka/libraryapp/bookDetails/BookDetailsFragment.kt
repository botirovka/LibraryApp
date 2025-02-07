package com.botirovka.libraryapp.bookDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.databinding.FragmentBookDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookDetailsFragment : Fragment() {
    private lateinit var binding: FragmentBookDetailsBinding
    private val args: BookDetailsFragmentArgs by navArgs()
    private val viewModel: BookDetailsViewModel by viewModels(

    )


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

        binding.reviewsButton.setOnClickListener {
            findNavController().navigate(BookDetailsFragmentDirections.actionBookDetailsFragmentToReviewsFragment(args.bookId))
        }

        binding.editBookButton.setOnClickListener {
            findNavController().navigate(BookDetailsFragmentDirections.actionBookDetailsFragmentToEditBookFragment(args.bookId))
        }

        binding.closeAppButton.setOnClickListener {
           findNavController().navigate(R.id.action_global_areYouSureDialog)
        }
    }


    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.detailsContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.errorTextView.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.errorTextView.text = errorMessage
                binding.errorTextView.visibility = View.VISIBLE
                binding.detailsContainer.visibility = View.GONE
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }

        viewModel.bookDetails.observe(viewLifecycleOwner) { book ->
            if (book != null) {
                binding.bookTitleTextView.text = book.title
                binding.bookAuthorTextView.text = book.author
                binding.bookGenreTextView.text = book.genre.toString()
                binding.bookImageTextView.text = book.image
                binding.bookBorrowedCountTextView.text = book.borrowedCount.toString()
                binding.bookTotalBookCountTextView.text = book.totalBookCount.toString()
                binding.bookIsAvailableTextView.text = if(book.isAvailable)  "Available" else "Not Available"
                binding.bookLastBorrowedTimeTextView.text = book.lastBorrowedTime.toString()
                binding.bookIsFavoriteTextView.text = if (book.isFavorite) "This book is a favorite" else "This book is not a favorite"
                binding.detailsContainer.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
        }
    }


}