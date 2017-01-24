package com.noodle.sample;

import com.noodle.sample.model.Book;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@EBean(scope = EBean.Scope.Singleton)
public class BookManager {

  private List<Book> books = new ArrayList<>();

  public List<Book> getBooks() {
    return books;
  }

  public void addBook(Book book) {
    books.add(book);
  }
}
