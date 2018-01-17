package com.noodle.sample;

import android.app.Application;

import com.noodle.sample.data.BookManager;

public class NoodleApp extends Application {

  private BookManager bookManager;

  public BookManager getBookManager() {
    return bookManager;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    bookManager = new BookManager(this);
  }
}
