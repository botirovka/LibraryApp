package com.example.domain.extensions


class Extensions {

    companion object {

        fun com.example.domain.model.Book.toPrettyString(): String {
            return ("\n Book " + this.title +
                    "\n Author: " + this.author +
                    "\n Genre: " + this.genre +
                    "\n Image:" + this.image +
                    "\n Borrowed Count: " + this.borrowedCount +
                    "\n Is Available?: " + this.isAvailable +
                    "\n Last Borrowed Time: " + this.lastBorrowedTime)
        }

        fun List<com.example.domain.model.Book>.printPretty(): String {
            return this.joinToString { it.toPrettyString() }
        }

        fun List<com.example.domain.model.Book>.groupByGenre(): Map<com.example.domain.model.Genres, List<com.example.domain.model.Book>> {
            return this.groupBy { it.genre }
        }

        fun List<com.example.domain.model.Book>.sortedByTitleAscending(): List<com.example.domain.model.Book> {
            return this.sortedBy { it.title }
        }

        fun List<com.example.domain.model.Book>.sortedByTitleDescending(): List<com.example.domain.model.Book> {
            return this.sortedByDescending { it.title }
        }

        fun List<com.example.domain.model.Book>.sortedByAuthorAscending(): List<com.example.domain.model.Book> {
            return this.sortedBy { it.author }
        }

        fun List<com.example.domain.model.Book>.sortedByAuthorDescending(): List<com.example.domain.model.Book> {
            return this.sortedByDescending { it.author }
        }

        fun List<com.example.domain.model.Book>.sortedByTitleAvailableFirstAscending(): List<com.example.domain.model.Book> {
            val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

            val sortedAvailableBooks = availableBooks.sortedBy { it.title }
            val sortedNotAvailableBooks = notAvailableBooks.sortedBy { it.title }

            return sortedAvailableBooks + sortedNotAvailableBooks
        }

        fun List<com.example.domain.model.Book>.sortedByTitleAvailableFirstDescending(): List<com.example.domain.model.Book> {
            val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

            val sortedAvailableBooks = availableBooks.sortedByDescending { it.title }
            val sortedNotAvailableBooks = notAvailableBooks.sortedByDescending { it.title }

            return sortedAvailableBooks + sortedNotAvailableBooks
        }

        fun List<com.example.domain.model.Book>.sortedByAuthorAvailableFirstAscending(): List<com.example.domain.model.Book> {
            val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

            val sortedAvailableBooks = availableBooks.sortedBy { it.title }
            val sortedNotAvailableBooks = notAvailableBooks.sortedBy { it.title }

            return sortedAvailableBooks + sortedNotAvailableBooks
        }

        fun List<com.example.domain.model.Book>.sortedByAuthorAvailableFirstDescending(): List<com.example.domain.model.Book> {
            val (availableBooks, notAvailableBooks) = partition { it.isAvailable }

            val sortedAvailableBooks = availableBooks.sortedByDescending { it.author }
            val sortedNotAvailableBooks = notAvailableBooks.sortedByDescending { it.author }

            return sortedAvailableBooks + sortedNotAvailableBooks
        }


    }

}