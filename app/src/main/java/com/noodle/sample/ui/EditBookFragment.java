package com.noodle.sample.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.noodle.sample.NoodleApp;
import com.noodle.sample.R;
import com.noodle.sample.data.Book;
import com.noodle.sample.data.BookManager;

public class EditBookFragment extends AppCompatDialogFragment {

  public static final String ARG_BOOK_ID = "book_id";

  private EditText inputTitle;
  private EditText inputAuthor;
  private Button deleteButton;

  private BookManager bookManager;
  private Book book;
  private int title;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bookManager = ((NoodleApp) getActivity().getApplication()).getBookManager();

    final long id = getArguments() != null
        ? getArguments().getLong(ARG_BOOK_ID, 0)
        : 0;

    if (id > 0) {
      book = bookManager.getBook(id);
      title = R.string.edit_book;
    } else {
      book = new Book();
      title = R.string.add_book;
    }

  }

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_add_book, container, false);
  }

  @Override
  public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
    inputTitle = ((EditText) view.findViewById(R.id.inputTitle));
    inputAuthor = ((EditText) view.findViewById(R.id.inputAuthor));

    deleteButton = ((Button) view.findViewById(R.id.buttonDelete));
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        onDeleteClicked();
      }
    });
    deleteButton.setVisibility(book.id > 0 ? View.VISIBLE : View.GONE);

    view.findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        onSaveClicked();
      }
    });

    inputTitle.setText(book.title);
    inputAuthor.setText(book.authorName);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(final Bundle savedInstanceState) {
    final Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setTitle(title);
    return dialog;
  }

  private void onSaveClicked() {
    final String title = inputTitle.getText().toString().trim();
    final String author = inputAuthor.getText().toString().trim();

    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author)) {
      return;
    }

    book.title = title;
    book.authorName = author;

    bookManager.addBook(book);
    dismiss();
  }

  private void onDeleteClicked() {
    bookManager.deleteBook(book);
    dismiss();
  }

}
