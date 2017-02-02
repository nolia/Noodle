package com.noodle.sample.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.noodle.sample.R;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setTitle(R.string.main_title);

    findViewById(R.id.addBook).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        final EditBookFragment editBookFragment = new EditBookFragment();
        editBookFragment.show(getSupportFragmentManager(), "add_book");
      }
    });
  }

}
