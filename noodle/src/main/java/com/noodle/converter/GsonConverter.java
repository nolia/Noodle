package com.noodle.converter;

import com.google.gson.Gson;

/**
 *
 */
public class GsonConverter implements Converter {

  Gson gson;

  public GsonConverter(final Gson gson) {
    this.gson = gson;
  }

  @Override
  public <T> byte[] toBytes(final T t) {
    // TODO Reuse byte buffer for writing objects.
    return gson.toJson(t).getBytes();
  }

  @Override
  public <T> T fromBytes(final byte[] bytes, final Class<T> clazz) {
    return gson.fromJson(new String(bytes), clazz);
  }
}
