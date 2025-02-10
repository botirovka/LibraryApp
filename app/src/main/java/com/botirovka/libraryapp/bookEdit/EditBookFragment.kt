package com.botirovka.libraryapp.bookEdit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.databinding.FragmentEditBookBinding
import com.example.domain.model.ChangeBookRequest
import com.example.domain.model.Genres
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class EditBookFragment : Fragment() {
    private lateinit var binding: FragmentEditBookBinding
    private val args: EditBookFragmentArgs by navArgs()
    private val viewModel: EditBookViewModel by viewModels()
    val genresItems: List<String> = Genres.entries.map { it.name }
    private lateinit var genresDropMenu: AutoCompleteTextView
    private lateinit var adapterItems: ArrayAdapter<String>
    private var selectedGenre: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        genresDropMenu = binding.autoCompleteTV
        adapterItems = ArrayAdapter<String>(requireContext(), R.layout.genres_list_item,genresItems)
        genresDropMenu.setAdapter(adapterItems)

        if(savedInstanceState == null){
            viewModel.loadBookDetails(args.bookId)
        }

        observeViewModel()

        genresDropMenu.setOnItemClickListener {parent, _, position, _ ->
            selectedGenre = parent.getItemAtPosition(position) as String

        }

        binding.cancelEditBookButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.confirmEditBookButton.setOnClickListener {
            val title = binding.titleEditTextLayout.editText?.text.toString()
            val author = binding.authorEditTextLayout.editText?.text.toString()
            viewModel.changeBook(ChangeBookRequest(args.bookId, title, author, selectedGenre))
        }



    }



    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.editBookLinearLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                binding.errorTextView.text = errorMessage
                binding.errorTextView.visibility = View.VISIBLE
                binding.editBookLinearLayout.visibility = View.GONE

            } else {
                binding.errorTextView.visibility = View.GONE
            }
        }

        viewModel.bookDetails.observe(viewLifecycleOwner) { book ->
            if (book != null) {
                binding.bookTitleTextView.text = book.title
                binding.editBookLinearLayout.visibility = View.VISIBLE

            }
        }

        viewModel.changed.observe(viewLifecycleOwner){isChanged ->
            findNavController().popBackStack()
        }
    }



}