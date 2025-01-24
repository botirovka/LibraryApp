package com.botirovka.libraryapp.models


class Extensions {

 companion object{

     fun Book.toPrettyString(): String{
         return ("\n Book " + this.title +
                 "\n Author: " + this.author +
                 "\n Genre: " + this.genre +
                 "\n Image:" + this.image +
                 "\n Borrowed Count: " + this.borrowedCount +
                 "\n Is Available?: " + this.isAvailable +
                 "\n Last Borrowed Time: " + this.lastBorrowedTime)
     }

     fun List<Book>.printPretty(): String{
         return this.joinToString { it.toPrettyString() }
     }

     fun List<Book>.groupByGenre(): Map<Genres, List<Book>> {
         return this.groupBy { it.genre }
     }
     fun List<Book>.sortedByTitleAscending(): List<Book> {
         return this.sortedBy { it.title }
     }
     fun List<Book>.sortedByTitleDescending(): List<Book> {
         return this.sortedByDescending { it.title }
     }

     fun List<Book>.sortedByAuthorAscending(): List<Book> {
         return this.sortedBy { it.author }
     }

     fun List<Book>.sortedByAuthorDescending(): List<Book> {
         return this.sortedByDescending { it.author }
     }

     fun List<Book>.sortedByTitleAvailableFirstAscending(): List<Book> {
         val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

         val sortedAvailableBooks = availableBooks.sortedBy { it.title }
         val sortedNotAvailableBooks = notAvailableBooks.sortedBy { it.title }

         return sortedAvailableBooks + sortedNotAvailableBooks
     }

     fun List<Book>.sortedByTitleAvailableFirstDescending(): List<Book> {
         val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

         val sortedAvailableBooks = availableBooks.sortedByDescending { it.title }
         val sortedNotAvailableBooks = notAvailableBooks.sortedByDescending { it.title }

         return sortedAvailableBooks + sortedNotAvailableBooks
     }

     fun List<Book>.sortedByAuthorAvailableFirstAscending(): List<Book> {
         val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

         val sortedAvailableBooks = availableBooks.sortedBy { it.title }
         val sortedNotAvailableBooks = notAvailableBooks.sortedBy { it.title }

         return sortedAvailableBooks + sortedNotAvailableBooks
     }

     fun List<Book>.sortedByAuthorAvailableFirstDescending(): List<Book> {
         val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

         val sortedAvailableBooks = availableBooks.sortedByDescending { it.author }
         val sortedNotAvailableBooks = notAvailableBooks.sortedByDescending { it.author }

         return sortedAvailableBooks + sortedNotAvailableBooks
     }










 }

}