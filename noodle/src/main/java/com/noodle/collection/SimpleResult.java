package com.noodle.collection;

import com.noodle.Result;

import java.util.concurrent.Callable;

/**
 *
 */
class SimpleResult<K> implements Result<K> {


  private final Callable<K> callable;

  SimpleResult(Callable<K> callable) {
    this.callable = callable;
  }

  @Override
  public K now() {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
