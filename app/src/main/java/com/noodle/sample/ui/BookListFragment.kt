package com.noodle.sample.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.noodle.sample.NoodleApp
import com.noodle.sample.R
import com.noodle.sample.data.Book
import com.noodle.sample.data.BookManager
import java.util.*

class BookListFragment : Fragment() {

    private lateinit var bookManager: BookManager
    private lateinit var adapter: BookListAdapter

    private lateinit var recyclerView: RecyclerView

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookManager = (requireActivity().application as NoodleApp).bookManager
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.fragment_book_list, menu)
        val searchView = menu!!.findItem(R.id.search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterItems(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterItems(newText)
                return false
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_book_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = BookListAdapter(bookManager.books)
        recyclerView.adapter = adapter

        bookManager.setListener(object : BookManager.Listener {
            override fun onBooksChanged() {
                adapter.setItems(bookManager.books)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.clear) {
            onClear()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun filterItems(query: String) {
        if (TextUtils.isEmpty(query)) {
            adapter.setItems(bookManager.books)
            return
        }

        adapter.setItems(bookManager.search(query))
    }

    private fun onClear() {
        AlertDialog.Builder(requireContext())
                .setTitle(R.string.remove_all_title)
                .setMessage(R.string.remove_all_message)
                .setPositiveButton(R.string.yes) { _, _ -> bookManager.clear() }
                .setNegativeButton(R.string.no, null)
                .show()
    }

    internal inner class BookListAdapter(
            private var bookList: List<Book>
    ) : RecyclerView.Adapter<BookViewHolder>() {

        private val layoutInflater: LayoutInflater = LayoutInflater.from(requireContext())


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
            val view = layoutInflater.inflate(R.layout.item_book, parent, false)

            return BookViewHolder(view)
        }

        override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
            val book = bookList[position]

            holder.bookTitle.text = book.title
            holder.bookAuthor.text = book.authorName

            holder.itemView.setOnClickListener {
                val editFragment = EditBookFragment()
                val args = Bundle()
                args.putLong(EditBookFragment.ARG_BOOK_ID, book.id)
                editFragment.arguments = args

                editFragment.show(fragmentManager, "edit_book")
            }
        }

        override fun getItemCount(): Int {
            return bookList.size
        }

        fun setItems(items: List<Book>) {
            this.bookList = ArrayList(items)
            notifyDataSetChanged()
        }
    }

    internal inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var bookTitle: TextView = itemView.findViewById(R.id.bookTitle) as TextView
        var bookAuthor: TextView = itemView.findViewById(R.id.bookAuthor) as TextView

    }
}
