package com.botirovka.libraryapp.bookList

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.botirovka.libraryapp.R
import com.botirovka.libraryapp.databinding.ItemAuthorBinding
import com.botirovka.libraryapp.databinding.ItemBookBinding
import com.bumptech.glide.Glide
import com.example.domain.model.Book


class BookAdapter(
    private val onBorrowClick: (Book) -> Unit,
    private val onItemClick: (Book) -> Unit,
    private val onFavoriteClick: (Book) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    val BOOK_VIEW_TYPE = 1
    val AUTHOR_VIEW_TYPE = 2

    private var items: List<BookListItem> = emptyList()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<BookListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is BookListItem.BookItem -> BOOK_VIEW_TYPE
            is BookListItem.AuthorItem -> AUTHOR_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            BOOK_VIEW_TYPE -> {
                val binding =
                    ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BookViewHolder(binding)
            }

            AUTHOR_VIEW_TYPE -> {
                val binding =
                    ItemAuthorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AuthorViewHolder(binding)
            }

            else -> throw Exception("INVALID TYPE")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (val item = items[position]) {

            is BookListItem.AuthorItem -> {
                val binding = (holder as AuthorViewHolder).binding
                val author = item.author
                binding.authorTextView.text = author.name
                binding.totalBooksByAuthorTextView.text = author.totalBooksCount.toString()
                binding.ratingBar.rating = author.rating
                if (author.image.isNotEmpty()) {
                    binding.authorImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context)
                        .load(author.image)
                        .placeholder(R.drawable.ic_book)
                        .into(binding.authorImageView)
                } else {
                    binding.authorImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.ic_account)
                        .into(binding.authorImageView)
                }
            }

            is BookListItem.BookItem -> {
                val binding = (holder as BookViewHolder).binding
                val book = item.book
                binding.titleTextView.text = book.title
                binding.authorTextView.text = book.author
                binding.genreTextView.text = book.genre.name
                binding.availabilityTextView.text =
                    "Available: ${book.totalBookCount - book.borrowedCount}/${book.totalBookCount}"

                if (book.image.isNotEmpty()) {
                    binding.bookImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context)
                        .load(book.image)
                        .placeholder(R.drawable.ic_book)
                        .into(binding.bookImageView)
                } else {
                    binding.bookImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.ic_book)
                        .into(binding.bookImageView)
                }
                if (book.isFavorite) {
                    binding.favoriteImageView.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.orange_light
                        )
                    )
                } else {
                    binding.favoriteImageView.imageTintList =
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                holder.itemView.context,
                                R.color.black
                            )
                        )
                }

                binding.borrowButton.setOnClickListener {
                    onBorrowClick(book)
                }

                holder.itemView.setOnClickListener {
                    Log.d("mydebug", "onItemClic: $book")
                    onItemClick(book)
                }

                binding.favoriteImageView.setOnClickListener {
                    onFavoriteClick(book)
                }
            }
        }
    }


    override fun getItemCount() = items.size

    class BookViewHolder(val binding: ItemBookBinding) : ViewHolder(binding.root)
    class AuthorViewHolder(val binding: ItemAuthorBinding) : ViewHolder(binding.root)
}
