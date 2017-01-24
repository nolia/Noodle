package com.noodle.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

/**
 *
 */

@EFragment(R.layout.fragment_add_book)
public class AddBookFragment extends AppCompatDialogFragment {

  @Bean
  BookManager bookManager;

  @NonNull
  @Override
  public Dialog onCreateDialog(final Bundle savedInstanceState) {
    final Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.setTitle("Add book");
    return dialog;
  }

}
