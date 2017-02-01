package com.noodle.storage;

import java.util.List;

/**
 * Primary interface for storing entities.
 */
public interface Storage {

  /**
   * Puts the record to a storage. Record must have
   * both key and data objects to null. If the given records key
   * is already in storage, stored record will be replaced with this
   * one.
   *
   * @param record to put to a storage
   */
  void put(Record record);

  /**
   * Removes the record from a storage. If it's not present returns
   * null.
   *
   * @param key of the record to remove
   * @return deleted record or null if not found
   */
  Record remove(byte[] key);

  /**
   * Fetch record by given key. May return null if not found.
   *
   * @param key to fetch the record of
   * @return record or null
   */
  Record get(byte[] key);

  /**
   * Returns all keys, that have given prefix.
   *
   * @param prefix byte array prefix to search keys
   * @return List of keys, which has given prefix
   */
  List<byte[]> prefixedWith(byte[] prefix);

}
