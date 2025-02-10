package com.botirovka.libraryapp.bookDetails

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.botirovka.libraryapp.NavGraphDirections
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.databinding.FragmentBookDetailsBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookDetailsFragment : Fragment() {
    private lateinit var binding: FragmentBookDetailsBinding
    private val args: BookDetailsFragmentArgs by navArgs()
    private val viewModel: BookDetailsViewModel by viewModels()


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

        if(savedInstanceState == null){
            viewModel.loadBookDetails(args.bookId)
        }

        binding.reviewsButton.setOnClickListener {
            findNavController().navigate(BookDetailsFragmentDirections.actionBookDetailsFragmentToReviewsFragment(args.bookId))
        }

        binding.editBookButton.setOnClickListener {
            findNavController().navigate(BookDetailsFragmentDirections.actionBookDetailsFragmentToEditBookFragment(args.bookId))
        }

    }


    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingImageProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.loadingDetailsProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
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
                if (book.image.isNotEmpty()) {
                    binding.bookImageView.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(book.image)
                        .placeholder(R.drawable.book_icon)
                        .into(binding.bookImageView)
                } else {
                    binding.bookImageView.visibility = View.VISIBLE
                }
                binding.availableTextView.text = "${book.totalBookCount - book.borrowedCount}/${book.totalBookCount}"
                binding.detailsTextGroup.visibility = View.VISIBLE
                binding.headerContainer.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
        }
    }


}