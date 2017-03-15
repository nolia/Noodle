package com.noodle.sample.data;

import android.content.Context;

import com.google.gson.Gson;
import com.noodle.Noodle;
import com.noodle.collection.Collection;
import com.noodle.converter.GsonConverter;
import com.noodle.description.Description;
import com.noodle.encryption.NoEncryption;
import com.noodle.storage.RandomAccessFileStorage;

import java.io.File;
import java.util.List;

public class BookManager {

  public Noodle noodle;
  private Listener listener;
  private Collection<Book> collection;

  public BookManager(final Context context) {

    final RandomAccessFileStorage storage = new RandomAccessFileStorage(
        new File(context.getFilesDir().getAbsolutePath() + File.separator + "data.noodle"),
        new NoEncryption());

    noodle = new Noodle(context, storage, new GsonConverter(new Gson()))
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

  public List<Book> search(final String query) {
    return collection.filter(new Collection.Predicate<Book>() {
      @Override
      public boolean test(final Book book) {
        return book.title.contains(query)
            || book.authorName.contains(query);

      }
    }).now();
  }

  public interface Listener {
    void onBooksChanged();
  }
}
