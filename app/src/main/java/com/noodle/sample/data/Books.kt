package com.noodle.sample.data

import android.annotation.SuppressLint
import android.content.Context

import com.noodle.Id
import com.noodle.Noodle
import com.noodle.collection.Collection

class Book {

    @Id
    var id: Long = 0

    var title: String? = null
    var authorName: String? = null

    @SuppressLint("DefaultLocale")
    override fun toString(): String =
            String.format("(%3d | %15s -> %s", id, authorName, title)
}

class BookManager(context: Context) {

    private var noodle: Noodle = Noodle.Builder(context)
            .addType(Book::class.java, "books")
            .build()

    private var listener: Listener? = null
    private val collection: Collection<Book>

    val books: List<Book>
        get() = collection.allAsync.value()

    init {

        collection = noodle.collectionOf(Book::class.java, "books")
    }

    fun addBook(book: Book) {
        collection.putAsync(book).value()
        if (listener != null) {
            listener!!.onBooksChanged()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun clear() {
        collection.clearAsync().value()
        listener?.onBooksChanged()
    }

    fun getBook(id: Long): Book {
        return collection.getAsync(id).value()
    }

    fun deleteBook(book: Book) {
        collection.deleteAsync(book.id).value()

        listener?.onBooksChanged()
    }

    fun search(query: String): List<Book> {
        return collection.filterAsync { book -> book.title!!.contains(query) || book.authorName!!.contains(query) }.value()
    }

    interface Listener {
        fun onBooksChanged()
    }
}
