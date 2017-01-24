package com.noodle.sample;

import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

/**
 *
 */
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

  @Bean
  BookManager bookManager;

  @Click(R.id.addBook)
  void onAddBookClick() {
    AddBookFragment_.builder().build().show(getSupportFragmentManager(), "add book");
  }


}
