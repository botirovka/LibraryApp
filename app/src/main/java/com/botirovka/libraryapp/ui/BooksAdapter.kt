package com.botirovka.libraryapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.models.Book
import com.bumptech.glide.Glide


class BooksAdapter(
    private var books: List<Book>,
    private val onItemClick: (Book) -> Unit
) : RecyclerView.Adapter<BooksAdapter.BookViewHolder>() {

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookId: TextView = itemView.findViewById(R.id.book_id)
        val titleTextView: TextView = itemView.findViewById(R.id.book_title)
        val authorTextView: TextView = itemView.findViewById(R.id.book_author)
        val genreTextView: TextView = itemView.findViewById(R.id.book_genre)
        val imageView: ImageView = itemView.findViewById(R.id.book_image)
        val copiesCounterTextView: TextView = itemView.findViewById(R.id.copies_available_count)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(books[position])
                }
            }
        }
    }

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book_rv, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.bookId.text = book.id.toString()
        holder.titleTextView.text = book.title
        holder.authorTextView.text = book.author
        holder.genreTextView.text = book.genre.name
        holder.copiesCounterTextView.text = book.totalBookCount.toString()
        if (book.image.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(book.image)
                .into(holder.imageView)
        }
        else{
            Glide.with(holder.itemView.context)
                .load(R.drawable.book_icon)
                .into(holder.imageView)
        }

    }

    override fun getItemCount(): Int = books.size
}
