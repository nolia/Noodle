package com.noodle.collection;

import com.noodle.Key;
import com.noodle.Result;

import java.util.HashMap;
import java.util.List;

/**
 * Basic, simple collection, backed by hashmap.
 */
public class InMemoryCollection <T> implements Collection<T> {

  private final Class<T> clazz;
  private final HashMap<Long, T> dataMap = new HashMap<>();
  private long sequenceId = 1;

  public InMemoryCollection(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public Result<T> get(final long id) {
    return null;
  }

  @Override
  public Result<Key<T>> put(final T t) {
    return null;
  }

  @Override
  public Result<T> delete(final Key<T> key) {
    return null;
  }

  @Override
  public Result<List<T>> all() {
    return null;
  }

  @Override
  public Result<List<T>> filter(final Predicate<T> t) {
    return null;
  }

  class SimpleResult<K> implements Result<K> {

    private final K item;

    SimpleResult(K item) {
      this.item = item;
    }

    @Override
    public K now() {
      return item;
    }
  }
}
