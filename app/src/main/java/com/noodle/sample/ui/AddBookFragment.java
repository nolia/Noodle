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
import android.widget.EditText;

import com.noodle.sample.NoodleApp;
import com.noodle.sample.R;
import com.noodle.sample.data.Book;
import com.noodle.sample.data.BookManager;

public class AddBookFragment extends AppCompatDialogFragment {

  private EditText inputTitle;
  private EditText inputAuthor;

  private BookManager bookManager;

  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    bookManager = ((NoodleApp) getActivity().getApplication()).getBookManager();
  }

  @Nullable
  @Override
  public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_add_book, container, false);

    inputTitle = ((EditText) view.findViewById(R.id.inputTitle));
    inputAuthor = ((EditText) view.findViewById(R.id.inputAuthor));

    view.findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        onSaveClicked();
      }
    });
    return view;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(final Bundle savedInstanceState) {
    final Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setTitle("Add book");
    return dialog;
  }

  private void onSaveClicked() {
    final String title = inputTitle.getText().toString().trim();
    final String author = inputAuthor.getText().toString().trim();
    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(author)) {
      return;
    }

    bookManager.addBook(new Book(title, author));
    dismiss();
  }

}
