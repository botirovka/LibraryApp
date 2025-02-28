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
import com.botirovka.libraryapp.databinding.ToolbarBinding
import com.bumptech.glide.Glide
import com.example.domain.model.Author
import com.example.domain.model.Book


class BookAdapter(
    private val onBorrowClick: (Book) -> Unit,
    private val onItemClick: (Book) -> Unit,
    private val onFavoriteClick: (Book) -> Unit,
    private val toolbar: ToolbarBinding
) : RecyclerView.Adapter<ViewHolder>() {

    val BOOK_VIEW_TYPE = 1
    val AUTHOR_VIEW_TYPE = 2

    private var items: List<BookListItem> = emptyList()
    private var authors: MutableList<Author> = mutableListOf()
    private var selectedItems: MutableMap<String, Set<Int>> = mutableMapOf()
    var isSelectMode = false


    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return when(val item = items[position]){
            is BookListItem.AuthorItem -> item.author.id.toLong()
            is BookListItem.BookItem -> item.book.id.toLong()
        }
    }

    fun getItemListIds(i: Int): Set<Int>{
        when(val item = items[i]){
            is BookListItem.BookItem -> return setOf(item.book.id)
            is BookListItem.AuthorItem ->{

                var index = items.indexOf(item) + 1
                while ((items[index] is BookListItem.AuthorItem).not()) {
                    when (val subItem = items[index]) {
                        is BookListItem.AuthorItem -> break
                        is BookListItem.BookItem -> if (subItem.book.author == item.author.name) {
                            selectedItems[subItem.book.author] =
                                selectedItems.getOrDefault(
                                    subItem.book.author, emptySet()
                                ) + subItem.book.id
                        }
                    }
                    index++
                    if (index == items.size) break
                }
                return selectedItems[item.author.name]?.plus(item.author.id)
                    ?: setOf( item.author.id)
            }
        }
    }

    fun getSelectedItems(): Set<Int> {
        Log.d("Adapter", "getSelectedItems: $authors")
        val selectedAuthorsId =
            authors.filter { (selectedItems[it.name]?.size ?: 0) == it.totalBooksCount }
                .map { it.id }.toSet()
        Log.d("Adapter", "getSelectedItems: $selectedAuthorsId")
        val selectedBooksId = selectedItems.values.flatten().toSet()
        return selectedBooksId + selectedAuthorsId
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

                val colorDefault = ContextCompat.getColor(
                    holder.itemView.context, R.color.bg_book_color
                )
                val colorChecked = ContextCompat.getColor(
                    holder.itemView.context, R.color.purple_light
                )

                val binding = (holder as AuthorViewHolder).binding
                val author = item.author
                authors.add(author)
                binding.checkboxSelectAuthor.isChecked = false
                binding.authorTextView.text = author.name
                binding.totalBooksByAuthorTextView.text = author.totalBooksCount.toString()
                binding.ratingBar.rating = author.rating
                Log.d("mydebugAdapter", "aUTHOR: $selectedItems")
                if (isSelectMode) {
                    Log.d("mydebugAdapter", "aUTHOR: ${selectedItems.keys}")
                    if (selectedItems.keys.contains(author.name)) {
                        Log.d("mydebugAdapter", "aUTHOR: key found}")
                        if ((selectedItems[author.name]?.size ?: 0) == author.totalBooksCount) {
                            Log.d("mydebugAdapter", "aUTHOR: size")
                            binding.checkboxSelectAuthor.isChecked = true
                            binding.authorContainerCL.background.setTint(colorChecked)
                        } else {
                            binding.checkboxSelectAuthor.isChecked = false
                            binding.authorContainerCL.background.setTint(colorDefault)
                        }
                    } else {
                        Log.d("mydebugAdapter", "aUTHOR: default")
                        binding.checkboxSelectAuthor.isChecked = false
                        binding.authorContainerCL.background.setTint(colorDefault)
                    }

                    binding.checkboxSelectAuthor.visibility = View.VISIBLE
                } else {
                    Log.d("mydebugAdapter", "noSelectMOde")
                    selectedItems = mutableMapOf()
                    binding.checkboxSelectAuthor.visibility = View.GONE
                    binding.authorContainerCL.background.setTint(colorDefault)
                }

                if (author.image.isNotEmpty()) {
                    binding.authorImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).load(author.image)
                        .placeholder(R.drawable.ic_book).into(binding.authorImageView)
                } else {
                    binding.authorImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).load(R.drawable.ic_account)
                        .into(binding.authorImageView)
                }

                holder.itemView.setOnClickListener {
                    if (isSelectMode) {
                        binding.checkboxSelectAuthor.isChecked =
                            binding.checkboxSelectAuthor.isChecked.not()
                        if (binding.checkboxSelectAuthor.isChecked) {

                            binding.authorContainerCL.background.setTint(colorChecked)
                            var index = items.indexOf(item) + 1
                            while ((items[index] is BookListItem.AuthorItem).not()) {
                                when (val subItem = items[index]) {
                                    is BookListItem.AuthorItem -> break
                                    is BookListItem.BookItem -> if (subItem.book.author == author.name) {
                                        selectedItems[subItem.book.author] =
                                            selectedItems.getOrDefault(
                                                subItem.book.author, emptySet()
                                            ) + subItem.book.id
                                    }
                                }
                                index++
                                if (index == items.size) break
                            }
                            notifyDataSetChanged()
                        } else {
                            selectedItems.keys.remove(author.name)
                            binding.authorContainerCL.background.setTint(colorDefault)

                            if (selectedItems.isEmpty()) {
                                toolbar.root.visibility = View.GONE
                                isSelectMode = false

                            }
                            notifyDataSetChanged()
                        }
                    }
                }

                holder.itemView.setOnLongClickListener {
                    toolbar.root.visibility = View.VISIBLE
                    binding.checkboxSelectAuthor.isChecked =
                        binding.checkboxSelectAuthor.isChecked.not()
                    if (binding.checkboxSelectAuthor.isChecked) {
                        binding.authorContainerCL.background.setTint(colorChecked)
                        var index = items.indexOf(item) + 1
                        Log.d("ADAPTER", "onBindViewHolder:$index ")
                        Log.d("ADAPTER", "onBindViewHolder:${items.size}")
                        if (index < items.size) {
                            while ((items[index] is BookListItem.AuthorItem).not()) {
                                when (val subItem = items[index]) {
                                    is BookListItem.AuthorItem -> break
                                    is BookListItem.BookItem -> if (subItem.book.author == author.name) {
                                        selectedItems[subItem.book.author] =
                                            selectedItems.getOrDefault(
                                                subItem.book.author, emptySet()
                                            ) + subItem.book.id
                                    }
                                }
                                if (index == items.size - 1) break
                                index++
                            }
                        }

                    } else {
                        selectedItems.keys.remove(author.name)
                        binding.authorContainerCL.background.setTint(colorDefault)
                    }
                    if (selectedItems.isEmpty()) {
                        toolbar.root.visibility = View.GONE
                        isSelectMode = false
                    } else {
                        isSelectMode = true
                    }
                    notifyDataSetChanged()
                    true
                }
            }

            is BookListItem.BookItem -> {
                val binding = (holder as BookViewHolder).binding
                val book = item.book
                binding.titleTextView.text = book.title
                binding.authorTextView.text = book.author
                binding.genreTextView.text = book.genre.name
                binding.checkboxSelectBook.isChecked = false
                binding.availabilityTextView.text =
                    "Available: ${book.totalBookCount - book.borrowedCount}/${book.totalBookCount}"

                val colorDefault = ContextCompat.getColor(
                    holder.itemView.context, R.color.bg_book_color
                )
                val colorChecked = ContextCompat.getColor(
                    holder.itemView.context, R.color.purple_light
                )


                if (isSelectMode) {
                    Log.d("mydebugAdapter", "onBindViewHolder: $selectedItems")
                    Log.d("mydebugAdapter", "onBindViewHolder: ${book.author}")
                    if (selectedItems[book.author]?.contains(book.id) == true) {

                        binding.checkboxSelectBook.isChecked = true
                        binding.bookContainerCL.background.setTint(colorChecked)
                    } else {
                        binding.bookContainerCL.background.setTint(colorDefault)
                    }

                    binding.checkboxSelectBook.visibility = View.VISIBLE
                } else {
                    selectedItems = mutableMapOf()
                    binding.checkboxSelectBook.visibility = View.GONE
                    binding.bookContainerCL.background.setTint(colorDefault)
                }

                if (book.image.isNotEmpty()) {
                    binding.bookImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).load(book.image)
                        .placeholder(R.drawable.ic_book).into(binding.bookImageView)
                } else {
                    binding.bookImageView.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).load(R.drawable.ic_book)
                        .into(binding.bookImageView)
                }
                if (book.isFavorite) {
                    binding.favoriteImageView.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            holder.itemView.context, R.color.orange_light
                        )
                    )
                } else {
                    binding.favoriteImageView.imageTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(
                            holder.itemView.context, R.color.black
                        )
                    )
                }

                holder.itemView.setOnClickListener {
                    if (isSelectMode) {
                        binding.checkboxSelectBook.isChecked =
                            binding.checkboxSelectBook.isChecked.not()
                        if (binding.checkboxSelectBook.isChecked) {
                            selectedItems[book.author] =
                                selectedItems.getOrDefault(book.author, emptySet()) + book.id
                            binding.bookContainerCL.background.setTint(colorChecked)
                            notifyDataSetChanged()
                        } else {
                            selectedItems[book.author] =
                                selectedItems[book.author]?.minus(book.id).orEmpty()
                            binding.bookContainerCL.background.setTint(colorDefault)
                            Log.d("mydebugAdapter", "${selectedItems.values}")
                            if (selectedItems.values.flatten().isEmpty()) {
                                toolbar.root.visibility = View.GONE
                                isSelectMode = false
                            }
                            notifyDataSetChanged()
                        }
                    } else {
                        onItemClick(book)
                    }
                }

                holder.itemView.setOnLongClickListener {
                    toolbar.root.visibility = View.VISIBLE
                    binding.checkboxSelectBook.isChecked =
                        binding.checkboxSelectBook.isChecked.not()
                    if (binding.checkboxSelectBook.isChecked) {
                        selectedItems[book.author] =
                            selectedItems.getOrDefault(book.author, emptySet()) + book.id
                        binding.bookContainerCL.background.setTint(colorChecked)
                    } else {
                        selectedItems[book.author] =
                            selectedItems[book.author]?.minus(book.id).orEmpty()
                        binding.bookContainerCL.background.setTint(colorDefault)
                    }
                    if (selectedItems.values.flatten().isEmpty()) {
                        toolbar.root.visibility = View.GONE
                        isSelectMode = false
                    } else {
                        isSelectMode = true
                    }
                    notifyDataSetChanged()
                    true
                }

                binding.favoriteImageView.setOnClickListener {
                   if(isSelectMode.not()) onFavoriteClick(book)
                   else holder.itemView.performClick()
                }

                binding.borrowButton.setOnClickListener {
                    if(isSelectMode.not()) onBorrowClick(book)
                    else holder.itemView.performClick()
                }
            }
        }
    }


    override fun getItemCount() = items.size

    fun notifyItemsDeleted() {
        toolbar.root.visibility = View.GONE
        isSelectMode = false
        selectedItems = mutableMapOf()
        notifyDataSetChanged()
    }


    class BookViewHolder(val binding: ItemBookBinding) : ViewHolder(binding.root)
    class AuthorViewHolder(val binding: ItemAuthorBinding) : ViewHolder(binding.root)
}
