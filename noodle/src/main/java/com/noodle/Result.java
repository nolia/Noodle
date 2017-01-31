package com.noodle;

import java.util.concurrent.Executor;

/**
 *
 */
public interface Result<T> {

  T now();

  Result<T> executeOn(Executor executor);

  Result<T> withCallback(Callback<T> callback);

  void get();

  interface Callback<T> {
    void onReady(T t);

    void onError(Exception e);
  }
}
