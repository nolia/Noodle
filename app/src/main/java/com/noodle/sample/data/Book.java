package com.noodle.sample.data;

import android.annotation.SuppressLint;

import com.noodle.Id;

public class Book {

  @Id
  public long id;

  public String title;
  public String authorName;

  @SuppressLint("DefaultLocale")
  @Override
  public String toString() {
    return String.format("(%3d | %15s -> %s", id, authorName, title);
  }
}
