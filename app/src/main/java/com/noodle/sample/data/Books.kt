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
            .addType(Book::class.java)
            .build()

    private var listener: Listener? = null
    private val collection: Collection<Book>

    val books: List<Book>
        get() = collection.all().now()

    init {

        collection = noodle.collectionOf(Book::class.java)
    }

    fun addBook(book: Book) {
        collection.put(book).now()
        if (listener != null) {
            listener!!.onBooksChanged()
        }
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun clear() {
        for (book in collection.all().now()) {
            collection.delete(book.id).now()
        }

        listener?.onBooksChanged()

    }

    fun getBook(id: Long): Book {
        return collection.get(id).now()
    }

    fun deleteBook(book: Book) {
        collection.delete(book.id).now()

        listener?.onBooksChanged()
    }

    fun search(query: String): List<Book> {
        return collection.filter { book -> book.title!!.contains(query) || book.authorName!!.contains(query) }.now()
    }

    interface Listener {
        fun onBooksChanged()
    }
}
