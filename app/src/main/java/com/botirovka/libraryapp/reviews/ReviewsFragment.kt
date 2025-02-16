package com.botirovka.libraryapp.reviews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.bookDetails.BookDetailsFragmentArgs
import com.botirovka.libraryapp.databinding.FragmentReviewsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewsFragment : Fragment() {
    private lateinit var binding: FragmentReviewsBinding
    private val args: BookDetailsFragmentArgs by navArgs()
    private val viewModel: ReviewsViewModel by viewModels()
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState == null){
            viewModel.loadReviewsByBookId(args.bookId)
        }
        reviewsRecyclerView = binding.rvReviews
        reviewAdapter = ReviewAdapter()
        reviewsRecyclerView.adapter = reviewAdapter
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvReviews.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.errorTextView.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.errorTextView.text = errorMessage
                binding.loadingProgressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
                binding.rvReviews.visibility = View.GONE
            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }

        viewModel.reviewsLiveData.observe(viewLifecycleOwner) { reviews ->
            if (reviews != null) {
                reviewAdapter.submitList(reviews)
                binding.rvReviews.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
        }
    }


}