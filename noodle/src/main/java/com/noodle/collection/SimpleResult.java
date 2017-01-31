package com.noodle.collection;

import com.noodle.Result;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 *
 */
class SimpleResult<K> implements Result<K> {

  private final Callable<K> action;
  Executor executor;
  Callback<K> callback;

  SimpleResult(Callable<K> action) {
    this.action = action;
  }

  @Override
  public K now() {
    try {
      return action.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Result<K> executeOn(final Executor executor) {
    this.executor = executor;
    return this;
  }

  @Override
  public Result<K> withCallback(final Callback<K> callback) {
    this.callback = callback;
    return this;
  }

  @Override
  public void get() {
    if (executor == null) {
      throw new RuntimeException("Executor is not specified!");
    }

    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          K k = now();
          if (callback != null) {
            callback.onReady(k);
          }
        } catch (Exception e) {
          if (callback != null) {
            callback.onError(e);
          }
        }
      }
    });
  }

}
