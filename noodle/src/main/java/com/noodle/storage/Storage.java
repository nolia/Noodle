package com.noodle.storage;

/**
 *
 */
public interface Storage {

  void put(Record record);

  Record remove(byte[] key);

  Record get(byte[] key);

}
