package com.botirovka.libraryapp.di

import com.example.data.repository.BookRepositoryImpl
import com.example.domain.repository.BookRepository
import com.example.domain.usecase.AddNewBookUseCase
import com.example.domain.usecase.BorrowBookUseCase
import com.example.domain.usecase.CreateNewBookUseCase
import com.example.domain.usecase.GetAllBooksUseCase
import com.example.domain.usecase.GetPaginatedBooksUseCase
import com.example.domain.usecase.SearchBooksUseCase
import com.example.domain.usecase.ToggleFavoriteBookUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun bindBookRepository(bookRepositoryImpl: BookRepositoryImpl): BookRepository
}

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {


    fun provideGetPaginatedBooksUseCase(bookRepository: BookRepository): GetPaginatedBooksUseCase {
        return GetPaginatedBooksUseCase(bookRepository)
    }


    fun provideGetAllBooksUseCase(bookRepository: BookRepository): GetAllBooksUseCase {
        return GetAllBooksUseCase(bookRepository)
    }

    fun provideBorrowBookUseCase(bookRepository: BookRepository): BorrowBookUseCase {
        return BorrowBookUseCase(bookRepository)
    }

    fun provideToggleFavoriteBookUseCase(bookRepository: BookRepository): ToggleFavoriteBookUseCase {
        return ToggleFavoriteBookUseCase(bookRepository)
    }

    fun provideAddNewBookUseCase(bookRepository: BookRepository): AddNewBookUseCase {
        return AddNewBookUseCase(bookRepository)
    }

    fun provideSearchBooksUseCase(bookRepository: BookRepository): SearchBooksUseCase {
        return SearchBooksUseCase(bookRepository)
    }

    fun provideCreateNewBookUseCase(bookRepository: BookRepository): CreateNewBookUseCase {
        return CreateNewBookUseCase(bookRepository)
    }
}