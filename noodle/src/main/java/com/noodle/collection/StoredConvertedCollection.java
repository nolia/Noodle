package com.noodle.collection;

import com.noodle.Result;
import com.noodle.Description;
import com.noodle.storage.Record;
import com.noodle.storage.Storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Primary {@link Collection} implementation.
 */
public class StoredConvertedCollection<T> implements Collection<T> {

  final Class<T> clazz;
  final Description<T> description;
  final Converter converter;
  final Storage storage;

  long sequenceId = 0;

  final Predicate<T> every = new Predicate<T>() {
    @Override
    public boolean test(final T t) {
      return true;
    }
  };

  public StoredConvertedCollection(final Class<T> clazz,
                                   final Description<T> description,
                                   final Converter converter,
                                   final Storage storage) {
    this.clazz = clazz;
    this.description = description;
    this.converter = converter;
    this.storage = storage;

    for (byte[] key : getAllCollectionKeys()) {
      final String idStr = new String(key).split(":")[1];
      final Long id = Long.valueOf(idStr);
      if (id != null && sequenceId < id) {
        sequenceId = id;
      }
    }
  }


  @Override
  public Result<T> get(final long id) {
    return new Result<>(new Callable<T>() {
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
    return new Result<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        putItemToCollection(t);

        return t;
      }
    });
  }

  @Override
  public Result<List<T>> putAll(final T[] all) {
    return new Result<>(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        if (all == null) {
          return Collections.emptyList();
        }

        for (T t : all) {
          putItemToCollection(t);
        }

        return Arrays.asList(all);
      }
    });
  }

  @Override
  public Result<List<T>> putAll(final Iterable<T> all) {
    return new Result<>(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        if (all == null) {
          return Collections.emptyList();
        }

        final ArrayList<T> list = new ArrayList<>();
        for (T t : all) {
          putItemToCollection(t);
          list.add(t);
        }

        return list;
      }
    });
  }

  @Override
  public Result<T> delete(final long id) {
    return new Result<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        final byte[] key = getKey(id);
        final Record removed = storage.remove(key);
        return removed != null
            ? converter.fromBytes(removed.getData(), clazz)
            : null;
      }
    });
  }

  @Override
  public Result<Boolean> deleteAll() {
    return new Result<>(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        for (byte[] key : getAllCollectionKeys()) {
          storage.remove(key);
        }
        return Boolean.TRUE;
      }
    });
  }

  @Override
  public Result<Integer> count() {
    return new Result<>(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return getAllCollectionKeys().size();
      }
    });
  }

  @Override
  public Result<List<T>> all() {
    return new Result<>(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        return findItemsWith(every);
      }
    });
  }

  @Override
  public Result<List<T>> filter(final Predicate<T> predicate) {
    return new Result<>(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        return findItemsWith(predicate);
      }
    });
  }

  private void putItemToCollection(T t) {
    long id = description.idOfItem(t);
    if (id == 0) {
      id = newSequenceId();
      description.setItemId(t, id);
    }
    storage.put(toRecord(id, t));
  }

  private ArrayList<T> findItemsWith(final Predicate<T> predicate) {
    final List<byte[]> keys = getAllCollectionKeys();
    final ArrayList<T> result = new ArrayList<>();
    for (int i = 0; i < keys.size(); i++) {
      final Record record = storage.get(keys.get(i));
      final T t = converter.fromBytes(record.getData(), clazz);
      if (predicate.test(t)) {
        result.add(t);
      }
    }
    return result;
  }

  private List<byte[]> getAllCollectionKeys() {
    return storage.prefixedWith(String.format(Locale.US, "%s", clazz.getCanonicalName()).getBytes());
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
