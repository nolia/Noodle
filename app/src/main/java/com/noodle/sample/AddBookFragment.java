package com.noodle.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.widget.EditText;

import com.noodle.sample.model.Book;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 *
 */

@EFragment(R.layout.fragment_add_book)
public class AddBookFragment extends AppCompatDialogFragment {

  @ViewById(R.id.inputTitle)
  EditText inputTitle;

  @ViewById(R.id.inputAuthor)
  EditText inputAuthor;

  @Bean
  BookManager bookManager;

  @Click(R.id.buttonSave)
  void onSaveClicked() {
    final Book book = new Book();
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

  @NonNull
  @Override
  public Dialog onCreateDialog(final Bundle savedInstanceState) {
    final Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setTitle("Add book");
    return dialog;
  }

}
