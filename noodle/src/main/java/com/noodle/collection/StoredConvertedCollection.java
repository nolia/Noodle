package com.noodle.collection;

import com.noodle.Result;
import com.noodle.converter.Converter;
import com.noodle.description.Description;
import com.noodle.storage.Record;
import com.noodle.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 *
 */

public class StoredConvertedCollection<T> implements Collection<T> {

  final Class<T> clazz;
  final Description<T> description;
  final Converter converter;
  final Storage storage;

  long sequenceId = 0;

  public StoredConvertedCollection(final Class<T> clazz,
                                   final Description<T> description,
                                   final Converter converter,
                                   final Storage storage) {
    this.clazz = clazz;
    this.description = description;
    this.converter = converter;
    this.storage = storage;
  }


  @Override
  public Result<T> get(final long id) {
    return new SimpleResult<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        final byte[] key = getKey(id);
        final Record record = storage.get(key);
        if (record == null) {
          return null;
        }

        return converter.fromBytes(record.getData(), clazz);
      }
    });
  }

  @Override
  public Result<T> put(final T t) {
    return new SimpleResult<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        long id = description.idOfItem(t);
        if (id == 0) {
          id = newSequenceId();
          description.setItemId(t, id);
        }
        storage.put(toRecord(id, t));

        return t;
      }
    });
  }

  @Override
  public Result<T> delete(final long id) {
    return null;
  }

  @Override
  public Result<List<T>> all() {
    return new SimpleResult<>(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        final List<byte[]> keys = storage.prefixedWith(String.format(Locale.US, "%s", clazz.getCanonicalName()).getBytes());
        final ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
          final Record record = storage.get(keys.get(i));
          result.add(converter.fromBytes(record.getData(), clazz));
        }

        return result;
      }
    });
  }

  @Override
  public Result<List<T>> filter(final Predicate<T> t) {
    return null;
  }

  private Record toRecord(final long id, final T t) {
    final byte[] key = getKey(id);
    final byte[] data = converter.toBytes(t);

    return new Record(key, data);
  }

  private byte[] getKey(final long id) {
    final String keyString = String.format(Locale.US,
        "%s:%d",
        clazz.getCanonicalName(),
        id);
    return keyString.getBytes();
  }

  private synchronized long newSequenceId() {
    sequenceId += 1;
    return sequenceId;
  }
}
