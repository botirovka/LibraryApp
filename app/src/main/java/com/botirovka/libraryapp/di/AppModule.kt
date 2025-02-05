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
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(bookRepositoryImpl: BookRepositoryImpl): BookRepository
}

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideGetPaginatedBooksUseCase(bookRepository: BookRepository): GetPaginatedBooksUseCase {
        return GetPaginatedBooksUseCase(bookRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetAllBooksUseCase(bookRepository: BookRepository): GetAllBooksUseCase {
        return GetAllBooksUseCase(bookRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideBorrowBookUseCase(bookRepository: BookRepository): BorrowBookUseCase {
        return BorrowBookUseCase(bookRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideToggleFavoriteBookUseCase(bookRepository: BookRepository): ToggleFavoriteBookUseCase {
        return ToggleFavoriteBookUseCase(bookRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideAddNewBookUseCase(bookRepository: BookRepository): AddNewBookUseCase {
        return AddNewBookUseCase(bookRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideSearchBooksUseCase(bookRepository: BookRepository): SearchBooksUseCase {
        return SearchBooksUseCase(bookRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCreateNewBookUseCase(bookRepository: BookRepository): CreateNewBookUseCase {
        return CreateNewBookUseCase(bookRepository)
    }
}