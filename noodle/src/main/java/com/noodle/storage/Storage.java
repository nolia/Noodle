package com.noodle.storage;

import java.util.List;

/**
 *
 */
public interface Storage {

  void put(Record record);

  Record remove(byte[] key);

  Record get(byte[] key);

  List<byte[]> prefixedWith(byte[] prefix);

}
