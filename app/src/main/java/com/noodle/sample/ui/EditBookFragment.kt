package com.noodle.sample.ui

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

import com.noodle.sample.NoodleApp
import com.noodle.sample.R
import com.noodle.sample.data.Book
import com.noodle.sample.data.BookManager

class EditBookFragment : AppCompatDialogFragment() {

    private lateinit var inputTitle: EditText
    private lateinit var inputAuthor: EditText
    private lateinit var deleteButton: Button

    private lateinit var bookManager: BookManager

    private lateinit var book: Book
    private var title: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookManager = (activity.application as NoodleApp).bookManager

        val id = if (arguments != null) {
            arguments.getLong(ARG_BOOK_ID, 0)
        } else {
            0
        }

        if (id > 0) {
            book = bookManager.getBook(id)
            title = R.string.edit_book
        } else {
            book = Book()
            title = R.string.add_book
        }

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_edit_book, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (view == null) {
            throw IllegalArgumentException("View may not be null!")
        }
        inputTitle = view.findViewById(R.id.inputTitle) as EditText
        inputAuthor = view.findViewById(R.id.inputAuthor) as EditText

        deleteButton = view.findViewById(R.id.buttonDelete) as Button
        deleteButton.setOnClickListener { onDeleteClicked() }
        deleteButton.visibility = if (book.id > 0) View.VISIBLE else View.GONE

        view.findViewById(R.id.buttonSave).setOnClickListener { onSaveClicked() }

        inputTitle.setText(book.title)
        inputAuthor.setText(book.authorName)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setTitle(title)
        return dialog
    }

    private fun onSaveClicked() {
        val title = inputTitle.text.toString().trim { it <= ' ' }
        val author = inputAuthor.text.toString().trim { it <= ' ' }

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author)) {
            return
        }

        book.title = title
        book.authorName = author

        bookManager.addBook(book)
        dismiss()
    }

    private fun onDeleteClicked() {
        bookManager.deleteBook(book)
        dismiss()
    }

    companion object {

        val ARG_BOOK_ID = "book_id"
    }

}
