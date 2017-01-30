package com.noodle;

import android.content.Context;

import com.google.gson.Gson;
import com.noodle.collection.Collection;
import com.noodle.collection.StoredConvertedCollection;
import com.noodle.converter.Converter;
import com.noodle.converter.GsonConverter;
import com.noodle.description.Description;
import com.noodle.storage.FileMappedBufferStorage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author Nikolay Soroka - Stanfy (http://stanfy.com)
 */
public class Noodle {

  private final Context context;
  private final String path;
  private final File file;
  private final FileMappedBufferStorage storage;

  final HashMap<String, Collection> collectionHashMap = new HashMap<>();
  final HashMap<String, Description> descriptionHashMap = new HashMap<>();
  final Converter converter;

  public Noodle(final Context context) {
    this(context, context.getFilesDir().getAbsolutePath() + File.separator + "data.noodle",
        new GsonConverter(new Gson()));
  }

  public Noodle(final Context context, final String path, final Converter converter) {
    this.context = context;
    this.path = path;
    this.file = new File(path);
    this.converter = converter;

    try {
      storage = new FileMappedBufferStorage(file);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public <T> Noodle registerType(final Class<T> type, final Description<T> description) {
    descriptionHashMap.put(type.getCanonicalName(), description);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> Collection<T> collectionOf(final Class<T> type) {
    final String kind = type.getCanonicalName();
    if (!descriptionHashMap.containsKey(kind)) {
      throw new IllegalArgumentException("Class is not registered: " + type.getCanonicalName());
    }
    Description<T> description = ((Description<T>) descriptionHashMap.get(kind));

    if (collectionHashMap.containsKey(kind)) {
      final Collection collection = collectionHashMap.get(kind);
      return ((Collection<T>) collection);
    }

    Collection<T> result = new StoredConvertedCollection<>(type, description, converter, storage);
    collectionHashMap.put(kind, result);

    return result;
  }

}
