package com.botirovka.libraryapp.apiUI

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.botirovka.libraryapp.databinding.FragmentApiBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ApiFragment : Fragment() {

    private val viewModel: ApiViewModel by viewModels()
    private lateinit var binding: FragmentApiBinding
    private var counter = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentApiBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.apiResult.observe(viewLifecycleOwner) { result ->
                    when {
                        result.isSuccess -> {
                            val data = result.getOrNull()
                            binding.resultTextView.text = "Success:\n$data"
                            updateButtonsText()
                        }
                        result.isFailure -> {
                            val error = result.exceptionOrNull()?.message ?: "Unknown error"
                            binding.resultTextView.text = "Error:\n$error"
                        }
                    }
                }
            }
        }

        viewModel.loadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.resultTextView.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.getPostsButton.setOnClickListener {
            viewModel.getPosts()
        }
        binding.getPostByIdButton.setOnClickListener {
            viewModel.getPostById(counter)
            counter++
        }
        binding.getCommentsButton.setOnClickListener {
            viewModel.getCommentsByPostId(counter)
            counter++
        }
        binding.createPostButton.setOnClickListener {
            viewModel.createPost()
        }
        binding.updatePostButton.setOnClickListener {
            viewModel.updatePost(counter)
            counter++
        }
        binding.deletePostButton.setOnClickListener {
            viewModel.deletePost(counter)
            counter++
        }
        binding.authButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            viewModel.login(username, password)
        }
        binding.getProfileInfoButton.setOnClickListener {
            viewModel.getProfileInfo()
        }
        binding.logOutButton.setOnClickListener {
            viewModel.logOut()
            binding.resultTextView.text = "Success: LogOut"
        }
    }


    private fun updateButtonsText(){
        binding.getPostByIdButton.text = "Get Post by ID $counter"
        binding.getCommentsButton.text = "Get Comments Post ID $counter"
        binding.updatePostButton.text = "Update Post ID $counter"
        binding.deletePostButton.text = "Delete Post ID $counter"
    }


}