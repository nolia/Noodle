package com.noodle.collection;

import com.noodle.Key;
import com.noodle.Result;

import java.util.List;

/**
 *
 */

public interface Collection<T> {

  Result<T> get(long id);

  Result<Key<T>> put(T t);

  Result<T> delete(Key<T> key);

  Result<List<T>> all();

  Result<List<T>> filter(Predicate<T> t);

  interface Predicate<T> {
    boolean satisfy(T t);
  }
}
