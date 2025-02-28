package com.botirovka.libraryapp.reviews

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.bookDetails.BookDetailsFragmentArgs
import com.botirovka.libraryapp.bookList.BooksMVVMFragmentDirections
import com.botirovka.libraryapp.databinding.FragmentReviewsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReviewsFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentReviewsBinding
    private val args: BookDetailsFragmentArgs by navArgs()
    private val viewModel: ReviewsViewModel by viewModels()
    private lateinit var reviewsRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter

    companion object {
        private const val ARG_BOOK_ID = "bookId"

        fun newInstance(bookId: Int): ReviewsFragment {
            val args = Bundle().apply {
                putInt(ARG_BOOK_ID, bookId)
            }
            return ReviewsFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.loadReviewsByBookId(args.bookId)
        }
        reviewsRecyclerView = binding.rvReviews
        reviewAdapter = ReviewAdapter()
        reviewsRecyclerView.adapter = reviewAdapter
        observeViewModel()
        updateBottomSheetHeight()
        addStickyButtonToBottomSheet()
    }

    private fun addStickyButtonToBottomSheet() {
        val density = requireContext().resources.displayMetrics.density
        dialog?.let {
            val coordinator =
                (it as BottomSheetDialog).findViewById<CoordinatorLayout>(com.google.android.material.R.id.coordinator)
            val containerLayout =
                it.findViewById<FrameLayout>(com.google.android.material.R.id.container)
            val btnGoToBookDetails = it.layoutInflater.inflate(R.layout.sticky_button, null)

            btnGoToBookDetails.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                height = (60 * density).toInt()
                gravity = Gravity.BOTTOM
            }

            containerLayout?.addView(btnGoToBookDetails)

            btnGoToBookDetails.post {
                val bottomMargin = (btnGoToBookDetails.measuredHeight - 8 * density).toInt()
                (coordinator?.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                    this.bottomMargin = bottomMargin
                    containerLayout?.requestLayout()
                }
            }

            btnGoToBookDetails.findViewById<Button>(R.id.button).setOnClickListener {
                findNavController().navigate(
                    BooksMVVMFragmentDirections.actionBooksMVVMFragmentToBookDetailsFragment(
                        args.bookId
                    )
                )
                Log.d("Sticky", "onViewCreated: click")
            }
        }
    }

    private fun updateBottomSheetHeight() {
        view?.viewTreeObserver?.addOnGlobalLayoutListener {
            val rowHeight =
                binding.reviewsTitleTextView.measuredHeight + binding.reviewEditTextLayout.measuredHeight
            val bottomSheet = binding.coordinatorLayout.parent as? View
            val bottomSheetBehavior =
                BottomSheetBehavior.from(bottomSheet ?: return@addOnGlobalLayoutListener)
            bottomSheetBehavior.peekHeight = rowHeight
        }
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