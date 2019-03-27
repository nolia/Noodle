package com.noodle.collection;

import com.noodle.Call;
import com.noodle.Description;
import com.noodle.storage.Record;
import com.noodle.storage.Storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Primary {@link Collection} implementation.
 */
public class StoredConvertedCollection<T> implements Collection<T> {

  final Class<T> clazz;
  final Description<T> description;
  final Converter converter;
  final Storage storage;

  final AtomicLong sequenceId = new AtomicLong(0L);

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
      if (sequenceId.get() < id) {
        sequenceId.set(id);
      }
    }
  }


  //region sync methods

  @Override
  public List<T> getAll() {
    return findItemsWith(every);
  }

  @Override
  public List<T> filter(final Predicate<T> predicate) {
    return findItemsWith(predicate);
  }

  @Override
  public T get(final long id) {
    final byte[] key = getKey(id);
    final Record record = storage.get(key);
    if (record == null) {
      return null;
    }
    return converter.fromBytes(record.getData(), clazz);
  }

  @Override
  public T put(final T t) {
    return putItemToCollection(t);
  }

  @SafeVarargs
  @Override
  public final List<T> putAll(final T... all) {
    return putAll(Arrays.asList(all));
  }

  @Override
  public List<T> putAll(final Iterable<T> all) {
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

  @Override
  public T delete(final long id) {
    final byte[] key = getKey(id);
    final Record removed = storage.remove(key);
    return removed != null
        ? converter.fromBytes(removed.getData(), clazz)
        : null;
  }

  @Override
  public boolean clear() {
    for (byte[] key : getAllCollectionKeys()) {
      storage.remove(key);
    }
    return true;
  }

  @Override
  public int count() {
    return getAllCollectionKeys().size();
  }


  //endregion

  @Override
  public Call<List<T>> getAllAsync() {
    return new Call<>(new Callable<List<T>>() {
      @Override
      public List<T> call() {
        return getAll();
      }
    });
  }

  @Override
  public Call<List<T>> filterAsync(final Predicate<T> predicate) {
    return new Call<>(new Callable<List<T>>() {
      @Override
      public List<T> call() {
        return filter(predicate);
      }
    });
  }

  @Override
  public Call<T> getAsync(final long id) {
    return new Call<>(new Callable<T>() {
      @Override
      public T call() {
        return get(id);
      }
    });
  }

  @Override
  public Call<T> putAsync(final T t) {
    return new Call<>(new Callable<T>() {
      @Override
      public T call() {
        return put(t);
      }
    });
  }

  @Override
  public Call<List<T>> putAllAsync(final T[] all) {
    return new Call<>(new Callable<List<T>>() {
      @Override
      public List<T> call() {
        return putAll(all);
      }
    });
  }

  @Override
  public Call<List<T>> putAllAsync(final Iterable<T> all) {
    return new Call<>(new Callable<List<T>>() {
      @Override
      public List<T> call() {
        return putAll(all);
      }
    });
  }

  @Override
  public Call<T> deleteAsync(final long id) {
    return new Call<>(new Callable<T>() {
      @Override
      public T call() {
        return delete(id);
      }
    });
  }

  @Override
  public Call<Boolean> clearAsync() {
    return new Call<>(new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return clear();
      }
    });
  }

  @Override
  public Call<Integer> countAsync() {
    return new Call<>(new Callable<Integer>() {
      @Override
      public Integer call() {
        return count();
      }
    });
  }

  private T putItemToCollection(T t) {
    long id = description.idOfItem(t);
    if (id == 0) {
      id = newSequenceId();
      t = description.setItemId(t, id);
    }
    storage.put(toRecord(id, t));
    return t;
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
    return storage.prefixedWith(description.getCollectionName().getBytes());
  }

  private Record toRecord(final long id, final T t) {
    final byte[] key = getKey(id);
    final byte[] data = converter.toBytes(t);

    return new Record(key, data);
  }

  private byte[] getKey(final long id) {
    final String keyString = String.format(Locale.US,
        "%s:%d",
        description.getCollectionName(),
        id);
    return keyString.getBytes();
  }

  private synchronized long newSequenceId() {
    return sequenceId.incrementAndGet();
  }
}
