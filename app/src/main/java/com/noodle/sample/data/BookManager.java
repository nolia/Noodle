package com.noodle.sample.data;

import android.content.Context;

import com.noodle.Noodle;
import com.noodle.collection.Collection;
import com.noodle.description.Description;

import java.util.List;

public class BookManager {

  private Noodle noodle;
  private Listener listener;
  private Collection<Book> collection;

  public BookManager(final Context context) {
    noodle = new Noodle(context)
        .registerType(Book.class, Description.of(Book.class).withIdField("id").build());

    collection = noodle.collectionOf(Book.class);
  }

  public List<Book> getBooks() {
    return noodle.collectionOf(Book.class).all().now();
  }

  public void addBook(Book book) {
    collection.put(book).now();
    if (listener != null) {
      listener.onBooksChanged();
    }
  }

  public void setListener(final Listener listener) {
    this.listener = listener;
  }

  public void clear() {
    for (Book book : collection.all().now()) {
      collection.delete(book.id).now();
    }

    if (listener != null) {
      listener.onBooksChanged();
    }
  }

  public Book getBook(final long id) {
    return collection.get(id).now();
  }

  public void deleteBook(final Book book) {
    collection.delete(book.id).now();
    if (listener != null) {
      listener.onBooksChanged();
    }
  }

  public interface Listener {
    void onBooksChanged();
  }
}
