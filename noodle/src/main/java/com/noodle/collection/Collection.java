package com.noodle.collection;

import com.noodle.Result;

import java.util.List;

/**
 *
 */

public interface Collection<T> {

  Result<T> get(long id);

  Result<T> put(T t);

  Result<T> delete(long id);

  Result<List<T>> all();

  Result<List<T>> filter(Predicate<T> t);

  interface Predicate<T> {
    boolean satisfy(T t);
  }
}
