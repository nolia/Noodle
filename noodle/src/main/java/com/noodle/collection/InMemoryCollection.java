package com.noodle.collection;

import com.noodle.Result;
import com.noodle.description.Description;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Basic, simple collection, backed by hashmap.
 */
public class InMemoryCollection<T> implements Collection<T> {

  final Class<T> clazz;
  final Description<T> description;

  final HashMap<Long, T> dataMap = new HashMap<>();
  long sequenceId = 0;

  public InMemoryCollection(Class<T> clazz, Description<T> description) {
    this.clazz = clazz;
    this.description = description;
  }

  @Override
  public Result<T> get(final long id) {
    return new SimpleResult<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        return !dataMap.containsKey(id) ? null : dataMap.get(id);
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
        dataMap.put(id, t);

        return t;
      }
    });
  }

  @Override
  public Result<T> delete(final long id) {
    return new SimpleResult<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        return dataMap.remove(id);
      }
    });
  }

  @Override
  public Result<List<T>> all() {
    return new SimpleResult<>(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        return Collections.unmodifiableList(new ArrayList<>(dataMap.values()));
      }
    });
  }

  @Override
  public Result<List<T>> filter(final Predicate<T> predicate) {
    return new SimpleResult<>(new Callable<List<T>>() {
      @Override
      public List<T> call() throws Exception {
        final ArrayList<T> results = new ArrayList<>();
        for (T item : dataMap.values()) {
          if (predicate.satisfy(item)) {
            results.add(item);
          }
        }
        return Collections.unmodifiableList(results);
      }
    });
  }

  private synchronized long newSequenceId() {
    sequenceId += 1;
    return sequenceId;
  }

}
