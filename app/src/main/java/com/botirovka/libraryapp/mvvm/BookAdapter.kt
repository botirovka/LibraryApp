package com.botirovka.libraryapp.mvvm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.data.Library
import com.botirovka.libraryapp.models.Book

import com.bumptech.glide.Glide

class BookAdapter(private val onClick: (Book) -> Unit) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var books: List<Book> = emptyList()

    fun submitList(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return BookViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val currentBook = books[position]
        holder.bind(currentBook)
    }

    override fun getItemCount() = books.size

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bookImageView: ImageView = itemView.findViewById(R.id.bookImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        private val genreTextView: TextView = itemView.findViewById(R.id.genreTextView)
        private val availabilityTextView: TextView = itemView.findViewById(R.id.availabilityTextView)
        private val borrowButton: Button = itemView.findViewById(R.id.borrowButton)

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = book.author
            genreTextView.text = book.genre.name
            availabilityTextView.text = "Available: ${book.totalBookCount - book.borrowedCount}/${book.totalBookCount}"

            if (book.image != null) {
                bookImageView.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(book.image)
                    .placeholder(R.drawable.book_icon)
                    .into(bookImageView)
            } else {
                bookImageView.visibility = View.GONE
            }

            borrowButton.setOnClickListener {
                onClick(book)
            }

        }


    }
}
