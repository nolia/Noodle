package com.noodle.sample.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.noodle.sample.NoodleApp;
import com.noodle.sample.R;
import com.noodle.sample.data.Book;
import com.noodle.sample.data.BookManager;

import java.util.ArrayList;
import java.util.List;

public class BookListFragment extends Fragment {

  BookManager bookManager;
  BookListAdapter adapter;

  RecyclerView recyclerView;

  public BookListFragment() {
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bookManager = ((NoodleApp) getActivity().getApplication()).getBookManager();
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_book_list, menu);
  }

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_book_list, container, false);
  }

  @Override
  public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
    recyclerView = ((RecyclerView) view.findViewById(R.id.recyclerView));
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    adapter = new BookListAdapter(bookManager.getBooks());
    recyclerView.setAdapter(adapter);

    bookManager.setListener(new BookManager.Listener() {
      @Override
      public void onBooksChanged() {
        adapter.setItems(bookManager.getBooks());
      }
    });
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == R.id.clear) {
      onClear();
    }
    return super.onOptionsItemSelected(item);
  }

  void onClear() {
    new AlertDialog.Builder(getActivity())
        .setTitle("Are you sure?")
        .setMessage("All items will be removed")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(final DialogInterface dialog, final int which) {
            bookManager.clear();
          }
        })
        .setNegativeButton("No", null)
        .show();
  }

  class BookListAdapter extends RecyclerView.Adapter<BookViewHolder> {

    List<Book> bookList = new ArrayList<>();
    LayoutInflater layoutInflater = getActivity().getLayoutInflater();

    BookListAdapter(final List<Book> bookList) {
      this.bookList = bookList;
    }

    @Override
    public BookViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
      final View view = layoutInflater.inflate(R.layout.item_book, parent, false);

      return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BookViewHolder holder, final int position) {
      holder.bookTitle.setText(bookList.get(position).title);
      holder.bookAuthor.setText(bookList.get(position).authorName);
    }

    @Override
    public int getItemCount() {
      return bookList.size();
    }

    void setItems(final List<Book> items) {
      this.bookList = new ArrayList<>(items);
      notifyDataSetChanged();
    }
  }

  class BookViewHolder extends RecyclerView.ViewHolder {

    TextView bookTitle;
    TextView bookAuthor;

    BookViewHolder(final View itemView) {
      super(itemView);
      bookTitle = ((TextView) itemView.findViewById(R.id.bookTitle));
      bookAuthor = ((TextView) itemView.findViewById(R.id.bookAuthor));
    }

  }
}
