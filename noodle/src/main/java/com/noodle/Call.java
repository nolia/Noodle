package com.noodle;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import io.reactivex.Observable;

/**
 * Represents result of operation upon a collection. The actual
 * item can be retrieved both sync and async.
 */
public class Call<K> {

  private final Callable<K> action;
  private Executor executor;

  /**
   * Creates new Call.
   *
   * @param action action to perform
   */
  public Call(Callable<K> action) {
    this.action = action;
  }

  /**
   * Synchronously runs all operations backing this result object
   * and returns the item.<br/>
   * <b>Note: this operation may throw {@link RuntimeException}. To be safe,
   * you can use callback and {@link #get(Callback)} method.</b>
   *
   * @return item, that this result holds
   */
  public K value() {
    try {
      return action.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets the {@link Executor} to run operation on.
   * Callback will be also called on this executor.
   *
   * @param executor executor to run operations on
   * @return this Call instance
   */
  public Call<K> executeOn(final Executor executor) {
    this.executor = executor;
    return this;
  }

  /**
   * Performs operations on specified executor.
   * Will throw a {@link RuntimeException} if executor was
   * not previously specified.
   *
   * @param callback callback to be called when result is ready
   * @return this Call instance
   */
  public Call<K> get(final Callback<K> callback) {
    if (executor == null) {
      throw new RuntimeException("Executor is not specified!");
    }

    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          K k = value();
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

    return this;
  }

  /**
   * Converts this result wrapper to rx observable.
   * <b>Caution: This method is optional and it relies on RxJava (version 2.x.y) to
   * be provided by the client</b>.
   *
   * @return Observable instance
   */
  public Observable<K> toRxObservable() {
    return Observable.fromCallable(action);
  }

  /**
   * Callback of the result.
   *
   * @param <T>
   */
  public interface Callback<T> {

    /**
     * Notified when result operation is ready.
     *
     * @param t result item
     */
    void onReady(T t);

    /**
     * Notified when there was an exception.
     *
     * @param e error that was thrown during get operation
     */
    void onError(Exception e);
  }
}
