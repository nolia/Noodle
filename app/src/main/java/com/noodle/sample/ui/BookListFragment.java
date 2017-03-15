package com.noodle.sample.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
    final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(final String query) {
        filterItems(query);
        return false;
      }

      @Override
      public boolean onQueryTextChange(final String newText) {
        filterItems(newText);
        return false;
      }
    });
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

  private void filterItems(final String query) {
    if (TextUtils.isEmpty(query)) {
      adapter.setItems(bookManager.getBooks());
      return;
    }

    adapter.setItems(bookManager.search(query));
  }

  void onClear() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.remove_all_title)
        .setMessage(R.string.remove_all_message)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(final DialogInterface dialog, final int which) {
            bookManager.clear();
          }
        })
        .setNegativeButton(R.string.no, null)
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
      final Book book = bookList.get(position);

      holder.bookTitle.setText(book.title);
      holder.bookAuthor.setText(book.authorName);

      holder.itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
          final EditBookFragment editFragment = new EditBookFragment();
          final Bundle args = new Bundle();
          args.putLong(EditBookFragment.ARG_BOOK_ID, book.id);
          editFragment.setArguments(args);

          editFragment.show(getFragmentManager(), "edit_book");
        }
      });
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
