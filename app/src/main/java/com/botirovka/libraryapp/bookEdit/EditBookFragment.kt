package com.botirovka.libraryapp.bookEdit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.databinding.FragmentEditBookBinding
import com.example.domain.usecase.GetBookByIdUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class EditBookFragment : Fragment() {
    @Inject
    lateinit var getBookByIdUseCase: GetBookByIdUseCase

    private lateinit var binding: FragmentEditBookBinding
    private val args: EditBookFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBookBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            setupUI()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeAppButton.setOnClickListener {
            findNavController().navigate(R.id.action_global_areYouSureDialog)
        }
    }

    private suspend fun setupUI() {
        val book = args.bookId.let { getBookByIdUseCase(it) }
        if (book != null) {
            binding.bookTitleTextView.text = book.title
        }
    }


}