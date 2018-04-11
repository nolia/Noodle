package com.noodle.collection;

import com.noodle.Call;
import com.noodle.Description;

import java.util.List;

/**
 * Collection is a generic interface for manipulating entities.
 * Usually you don't construct collections on your own, but use
 * {@link com.noodle.Noodle#collectionOf(Class)} method to get one.
 *
 * @see Call
 * @see Converter
 * @see Description
 */
public interface Collection<T> {

  /**
   * Get the item by it's id. Returns {@link Call},
   * holding this item or null, if not found.
   *
   * @param id of the given item, should be positive
   * @return {@link Call} that wraps item or null
   */
  Call<T> getAsync(long id);

  /**
   * Store the entity in the collection. If it does not have an id,
   * it will be stored as new, and assigned one. Otherwise old entity
   * will be replaced
   *
   * @param t entity to store
   * @return {@link Call} that wraps an updated item
   */
  Call<T> putAsync(T t);

  /**
   * Puts all elements with put.
   *
   * @param all items
   * @return {@link Call}, that holds actual result of this operation.
   */
  Call<List<T>> putAllAsync(T... all);

  /**
   * Puts all elements with put.
   *
   * @param all items
   * @return {@link Call}, that holds actual result of this operation.
   */
  Call<List<T>> putAllAsync(Iterable<T> all);

  /**
   * Delete stored entity by its id. If not found, does nothing,
   * returning null in the result.
   *
   * @param id of the item to delete
   * @return {@link Call} that wraps deleted item or null, if not found
   */
  Call<T> deleteAsync(long id);

  /**
   * Deletes all items from this collection.
   *
   * @return {@link Call}, that holds actual result of this operation.
   */
  Call<Boolean> clearAsync();

  /**
   * Counts all items in this collection.
   *
   * @return {@link Call}, that holds the number of items in this collection.
   */
  Call<Integer> countAsync();

  /**
   * Returns unmodifiable list of all items in this collection.
   *
   * @return {@link Call}, that holds unmodifiable list
   * of all items in this collection.
   */
  Call<List<T>> allAsync();

  /**
   * Returns unmodifiable list of all items in this collection,
   * that satisfy given predicate.
   *
   * @param predicate test function to filter items
   * @return {@link Call}, with unmodifiable list of all items,
   * that satisfy given predicate.
   */
  Call<List<T>> filterAsync(Predicate<T> predicate);

  /**
   * Test function, used to filter items.
   *
   * @param <T> type of objects to be tested.
   */
  interface Predicate<T> {
    boolean test(T t);
  }
}
