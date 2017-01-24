package com.noodle;

/**
 *
 */
public class Key<T> {

  private final Class<T> clazz;
  private final long id;

  Key(Class<T> clazz, long id) {
    this.clazz = clazz;
    this.id = id;
  }
}
