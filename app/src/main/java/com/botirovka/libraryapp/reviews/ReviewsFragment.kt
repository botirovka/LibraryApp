package com.botirovka.libraryapp.reviews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.botirovka.libraryapp.bookDetails.BookDetailsFragmentArgs
import com.botirovka.libraryapp.databinding.FragmentReviewsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewsFragment : Fragment() {
    private lateinit var binding: FragmentReviewsBinding
    private val args: BookDetailsFragmentArgs by navArgs()
    private val viewModel: ReviewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewsBinding.inflate(inflater, container, false)
        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.reviewTextView.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.errorTextView.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.errorTextView.text = errorMessage
                binding.errorTextView.visibility = View.VISIBLE
                binding.reviewTextView.visibility = View.GONE
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }

        viewModel.bookDetails.observe(viewLifecycleOwner) { book ->
            if (book != null) {
                binding.titleTextView.text = "Petro"
                binding.reviewTextView.text = "${book.title} is a good book."
                binding.ratingTextView.text = "${book.totalBookCount - book.borrowedCount}/${book.totalBookCount}"
                binding.reviewTextView.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
        }
    }


}