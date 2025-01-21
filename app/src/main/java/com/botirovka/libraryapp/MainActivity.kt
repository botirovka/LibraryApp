package com.botirovka.libraryapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.botirovka.libraryapp.databinding.ActivityMainBinding
import com.botirovka.libraryapp.ui.BooksFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNav: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI()


        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun setupUI() {
        replaceFragment(BooksFragment())
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNav = binding.bottomNav
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.add_book -> replaceFragment(BooksFragment())
                R.id.books -> replaceFragment(BooksFragment())
                R.id.borrowed_books -> replaceFragment(BooksFragment(), isBorrowed = true)
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment , isBorrowed: Boolean = false): Boolean {
        val bundle = Bundle()
        bundle.putBoolean("is_borrowed", isBorrowed)
        fragment.arguments = bundle

        supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}