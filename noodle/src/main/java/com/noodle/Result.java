package com.noodle;

import java.util.concurrent.Executor;

import io.reactivex.Observable;

/**
 * Represents result of operation upon a collection. The actual
 * item can be retrieved both sync and async.
 */
public interface Result<T> {

  /**
   * Synchronously runs all operations backing this result object
   * and returns the item.<br/>
   * <b>Note: this operation may throw {@link RuntimeException}. To be safe,
   * you can use callback and {@link #get()} method.</b>
   *
   * @return item, that this result holds
   */
  T now();

  /**
   * Sets the {@link Executor} to run {@link #get()} operation on.
   * Callback will be also called on this executor.
   *
   * @param executor executor to run operations on
   * @return this Result instance
   */
  Result<T> executeOn(Executor executor);

  /**
   * Sets a callback to this Result.
   *
   * @param callback callback to be called when result is ready
   * @return this Result instance
   */
  Result<T> withCallback(Callback<T> callback);

  /**
   * Performs operations on specified executor.
   * Will throw a {@link RuntimeException} if executor was
   * not previously specified .
   */
  void get();

  /**
   * Converts this result wrapper to rx observable.
   * <b>Caution: This method is optional and it relies on RxJava (version 2.x.y) to
   * be provided by the client</b>.
   * @return Observable instance
   */
  Observable<T> toRxObservable();

  /**
   * Callback of the result.
   * @param <T>
   */
  interface Callback<T> {

    /**
     * Notified when result operation is ready.
     * @param t result item
     */
    void onReady(T t);

    /**
     * Notified when there was an exception.
     * @param e error that was thrown during get operation
     */
    void onError(Exception e);
  }
}
