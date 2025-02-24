package com.botirovka.libraryapp.bookList


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Library
import com.example.domain.extensions.Extensions.Companion.toPrettyString
import com.example.domain.model.Book
import com.example.domain.usecase.AddNewBookUseCase
import com.example.domain.usecase.BorrowBookUseCase
import com.example.domain.usecase.CreateNewBookUseCase
import com.example.domain.usecase.DeleteAuthorsUseCase
import com.example.domain.usecase.DeleteBooksUseCase
import com.example.domain.usecase.SearchAuthorsUseCase
import com.example.domain.usecase.SearchBooksUseCase
import com.example.domain.usecase.ToggleFavoriteBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val borrowBookUseCase: BorrowBookUseCase,
    private val toggleFavoriteBookUseCase: ToggleFavoriteBookUseCase,
    private val addNewBookUseCase: AddNewBookUseCase,
    private val searchBooksUseCase: SearchBooksUseCase,
    private val createNewBookUseCase: CreateNewBookUseCase,
    private val searchAuthorsUseCase: SearchAuthorsUseCase,
    private val deleteBooksUseCase: DeleteBooksUseCase,
    private val deleteAuthorsUseCase: DeleteAuthorsUseCase
) : ViewModel() {

    private val _itemsLiveData = MutableLiveData<List<BookListItem>>(emptyList())
    val itemsLiveData: LiveData<List<BookListItem>> get() = _itemsLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> get() = _loadingLiveData

    private val _loadingMoreLiveData = MutableLiveData<Boolean>()
    val loadingMoreLiveData: LiveData<Boolean> get() = _loadingMoreLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> get() = _errorLiveData


    private val _bookUnavailableChannel = Channel<String>()
    val bookUnavailableFlow = _bookUnavailableChannel.receiveAsFlow()
    private var isDataLoaded = false
    private var isLoading: Boolean = false

    init {
        if (isDataLoaded.not()) {
            loadAllItems()
        }
    }

    fun loadAllItems(query: String = "") {
        viewModelScope.launch {
            isLoading = true

            if(query.isNotBlank()) {
                _loadingLiveData.value = true
                _errorLiveData.value = null
                val items = mutableListOf<BookListItem>()
                val itemsBooksByQuery = searchBooksUseCase.invoke(query).map { BookListItem.BookItem(it) }
                val itemsAuthorsByQuery = searchAuthorsUseCase.invoke(query).map { BookListItem.AuthorItem(it)}

                itemsAuthorsByQuery.forEach { itemAuthor ->
                    items += BookListItem.AuthorItem(itemAuthor.author)
                    val booksByAuthor = searchBooksUseCase(itemAuthor.author.name)
                    items += booksByAuthor.map { BookListItem.BookItem(it) }
                }

                val filteredItemsBooksByQuery = itemsBooksByQuery.filter { newItem ->
                    items.none { existing ->
                        (existing as? BookListItem.BookItem)?.book?.id == newItem.book.id
                    }
                }

                _itemsLiveData.value = items + filteredItemsBooksByQuery
                _loadingLiveData.value = false
                isLoading = false
            }

            else {
                _loadingLiveData.value = true
                _errorLiveData.value = null
                val items = mutableListOf<BookListItem>()
                val authors = Library.getAllAuthors()
                authors.forEach { author ->
                    items += BookListItem.AuthorItem(author)
                    val booksByAuthor = searchBooksUseCase(author.name)
                    items += booksByAuthor.map { BookListItem.BookItem(it) }
                }
                _loadingLiveData.value = false
                _itemsLiveData.value = items
                isLoading = false
            }
        }
    }


    fun createNewBook(){
        val newBook  = createNewBookUseCase()
        addNewBook(newBook)
    }

    private fun addNewBook(newBook: Book) {
        if (isLoading) return
        viewModelScope.launch {
            _loadingMoreLiveData.value = true
            val id = addNewBookUseCase(newBook)
            Log.d("mydebug", "addNewBook: $id")
            newBook.id = id
            Log.d("mydebug", "addNewBook: ${newBook.toPrettyString()}")
            val currentItems = _itemsLiveData.value?.toMutableList()
            currentItems?.let {
                if (currentItems.size > 0){
                    currentItems.removeAt(Random.nextInt(currentItems.size))
                }
                _itemsLiveData.value = currentItems + BookListItem.BookItem(newBook)
            }
            _loadingMoreLiveData.value = false
        }
    }

    fun borrowBook(book: Book) {
        viewModelScope.launch {
            val isBorrowed = borrowBookUseCase(book.title)
            if (isBorrowed) {
                _itemsLiveData.value = _itemsLiveData.value

            } else {
                _bookUnavailableChannel.send("Book '${book.title}' is not available")
            }
        }
    }

    fun changeBookFavoriteStatus(book: Book) {
        viewModelScope.launch {
            val isChanged = toggleFavoriteBookUseCase(book.id)
            if (isChanged) {
                _itemsLiveData.value = _itemsLiveData.value

            } else {
                _errorLiveData.value = "Unexpected error"
            }
        }
    }

    fun deleteItems(selectedItems: MutableSet<Int>) {
        deleteBooksUseCase(selectedItems)
        deleteAuthorsUseCase(selectedItems)
        _itemsLiveData.value = _itemsLiveData.value
            ?.filterNot { item ->
                item is BookListItem.BookItem && item.book.id in selectedItems
            }?.filterNot {item ->
                item is BookListItem.AuthorItem && item.author.id in selectedItems
            }


    }
        
    }