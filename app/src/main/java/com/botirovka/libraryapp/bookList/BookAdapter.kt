package com.botirovka.libraryapp.bookList

import android.annotation.SuppressLint
import android.content.res.ColorStateList
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
    private var selectedItems: MutableSet<Int> = mutableSetOf()
    var isSelectMode = false


    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    fun getSelectedItems() = selectedItems

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

                val colorDefault= ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.bg_book_color
                )
                val colorChecked = ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.purple_light
                )

                val binding = (holder as AuthorViewHolder).binding
                val author = item.author
                binding.checkboxSelectAuthor.isChecked = false
                binding.authorTextView.text = author.name
                binding.totalBooksByAuthorTextView.text = author.totalBooksCount.toString()
                binding.ratingBar.rating = author.rating

                if(isSelectMode){

                    if(selectedItems.contains(author.id)){
                        binding.checkboxSelectAuthor.isChecked = true
                        binding.authorContainerCL.background.setTint(colorChecked)
                    }
                    else{
                        binding.authorContainerCL.background.setTint(colorDefault)
                    }

                    binding.checkboxSelectAuthor.visibility = View.VISIBLE
                }
                else{
                    selectedItems = mutableSetOf()
                    binding.checkboxSelectAuthor.visibility = View.GONE
                    binding.authorContainerCL.background.setTint(colorDefault)
                }

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

                holder.itemView.setOnClickListener {
                    if(isSelectMode){
                        binding.checkboxSelectAuthor.isChecked = binding.checkboxSelectAuthor.isChecked.not()
                        if(binding.checkboxSelectAuthor.isChecked){
                            selectedItems.add(author.id)
                            binding.authorContainerCL.background.setTint(colorChecked)
                        }
                        else{
                            selectedItems.remove(author.id)
                            binding.authorContainerCL.background.setTint(colorDefault)

                            if(selectedItems.isEmpty()){
                                toolbar.root.visibility = View.GONE
                                isSelectMode = false
                                notifyDataSetChanged()
                            }
                        }
                    }
                }

                holder.itemView.setOnLongClickListener {
                    toolbar.root.visibility = View.VISIBLE
                    binding.checkboxSelectAuthor.isChecked = binding.checkboxSelectAuthor.isChecked.not()
                    if(binding.checkboxSelectAuthor.isChecked){
                        selectedItems.add(author.id)
                        binding.authorContainerCL.background.setTint(colorChecked)
                    }
                    else{
                        selectedItems.remove(author.id)
                        binding.authorContainerCL.background.setTint(colorDefault)
                    }
                    if(selectedItems.isEmpty()){
                        toolbar.root.visibility = View.GONE
                        isSelectMode = false
                    }
                    else{
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

                val colorDefault= ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.bg_book_color
                )
                val colorChecked = ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.purple_light
                )



                if(isSelectMode){

                    if(selectedItems.contains(book.id)){
                        binding.checkboxSelectBook.isChecked = true
                        binding.bookContainerCL.background.setTint(colorChecked)
                    }
                    else{
                        binding.bookContainerCL.background.setTint(colorDefault)
                    }

                    binding.checkboxSelectBook.visibility = View.VISIBLE
                }
                else{
                    selectedItems = mutableSetOf()
                    binding.checkboxSelectBook.visibility = View.GONE
                    binding.bookContainerCL.background.setTint(colorDefault)
                }

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
                    if(isSelectMode){
                        binding.checkboxSelectBook.isChecked = binding.checkboxSelectBook.isChecked.not()
                        if(binding.checkboxSelectBook.isChecked){
                            selectedItems.add(book.id)
                            binding.bookContainerCL.background.setTint(colorChecked)
                        }
                        else{
                            selectedItems.remove(book.id)
                            binding.bookContainerCL.background.setTint(colorDefault)

                            if(selectedItems.isEmpty()){
                                toolbar.root.visibility = View.GONE
                                isSelectMode = false
                                notifyDataSetChanged()
                            }
                        }
                    }
                    else{
                        onItemClick(book)
                    }
                }

                holder.itemView.setOnLongClickListener {
                    toolbar.root.visibility = View.VISIBLE
                    binding.checkboxSelectBook.isChecked = binding.checkboxSelectBook.isChecked.not()
                    if(binding.checkboxSelectBook.isChecked){
                        selectedItems.add(book.id)
                        binding.bookContainerCL.background.setTint(colorChecked)
                    }
                    else{
                        selectedItems.remove(book.id)
                        binding.bookContainerCL.background.setTint(colorDefault)
                    }
                    if(selectedItems.isEmpty()){
                        toolbar.root.visibility = View.GONE
                        isSelectMode = false
                    }
                    else{
                        isSelectMode = true
                    }
                    notifyDataSetChanged()
                    true
                }

                binding.favoriteImageView.setOnClickListener {
                    onFavoriteClick(book)
                }
            }
        }
    }


    override fun getItemCount() = items.size

    fun notifyItemsDeleted() {
        toolbar.root.visibility = View.GONE
        isSelectMode = false
        selectedItems = mutableSetOf()
        notifyDataSetChanged()
    }

    class BookViewHolder(val binding: ItemBookBinding) : ViewHolder(binding.root)
    class AuthorViewHolder(val binding: ItemAuthorBinding) : ViewHolder(binding.root)
}
