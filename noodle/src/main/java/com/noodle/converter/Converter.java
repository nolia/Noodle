package com.noodle.converter;

/**
 *
 */

public interface Converter {

  <T> byte[] toBytes(T t);

  <T> T fromBytes(byte[] bytes, Class<T> clazz);
}
