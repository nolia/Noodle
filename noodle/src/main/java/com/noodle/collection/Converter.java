package com.noodle.collection;

/**
 * Converts between byte[] and actual Java objects and
 * vice versa.
 * @see GsonConverter
 */
public interface Converter {

  <T> byte[] toBytes(T t);

  <T> T fromBytes(byte[] bytes, Class<T> clazz);
}
