package com.noodle.sample.data;

public class Book {

  public long id;

  public String title;
  public String authorName;

  public Book(final String title, final String authorName) {
    this.title = title;
    this.authorName = authorName;
  }
}
