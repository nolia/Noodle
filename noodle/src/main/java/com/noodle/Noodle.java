package com.noodle;

import android.content.Context;

import com.google.gson.Gson;
import com.noodle.collection.Collection;
import com.noodle.collection.SimpleResult;
import com.noodle.collection.StoredConvertedCollection;
import com.noodle.converter.Converter;
import com.noodle.converter.GsonConverter;
import com.noodle.description.Description;
import com.noodle.storage.FileMappedBufferStorage;
import com.noodle.storage.Record;
import com.noodle.storage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Callable;

/**
 * Noodle is a lightweight super-simple persistence framework.
 * It does not have relations, columns, thread contained objects and indexes.
 * All data is accessed via {@link Collection} classes. All types of objects
 * that you want to store must be declared at construction time.
 * <br/>
 * Noodle stores all the data as byte arrays, and converts them to Java objects and
 * vice versa using {@link Converter} interface. The default converter is {@link GsonConverter},
 * but you can implement your own.
 * <br/>
 * Each stored entity has kind and id, which is represented as long primitive.
 */
public class Noodle {

  private final Context context;
  private final String path;
  private final File file;
  Storage storage;

  final HashMap<String, Collection> collectionHashMap = new HashMap<>();
  final HashMap<String, Description> descriptionHashMap = new HashMap<>();
  final Converter converter;

  /**
   * Create new Noodle with default settings.
   *
   * @param context should probably be application context
   */
  public Noodle(final Context context) {
    this(context, context.getFilesDir().getAbsolutePath() + File.separator + "data.noodle",
        new GsonConverter(new Gson()));
  }

  /**
   * Creates new Noodle with specified parameters.
   *
   * @param context application context
   * @param path path to the data file
   * @param converter which converter to use
   */
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

  /**
   * Registers the type of objects which can be stored in this Noodle storage.
   * If there was previously registered type, rewrites its description with new one.
   *
   * @param type type of objects to store
   * @param description description of the type
   * @param <T> generic type, to be statically type-safe
   * @return this Noodle instance
   */
  public <T> Noodle registerType(final Class<T> type, final Description<T> description) {
    descriptionHashMap.put(type.getCanonicalName(), description);
    return this;
  }

  /**
   * Returns the collection of the given type.<br/>
   * <b>Note: type description have to be already registered with
   * {@link #registerType(Class, Description)}.</b>
   *
   * @param type type of objects to store
   * @param <T> generic type, to be statically type-safe
   * @return collection of the given type.
   */
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

  public <T> Result<T> get(final String key, final Class<T> type) {
    return new SimpleResult<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        final byte[] keyBytes = keyValueKey(key);
        final Record record = storage.get(keyBytes);
        return record != null
            ? converter.fromBytes(record.getData(), type)
            : null;
      }
    });
  }

  public <T> Result<T> put(final String key, final T value) {
    return new SimpleResult<>(new Callable<T>() {
      @Override
      public T call() throws Exception {
        final byte[] keyBytes = keyValueKey(key);
        final byte[] data = converter.toBytes(value);
        storage.put(new Record(keyBytes, data));
        return value;
      }
    });
  }

  public Result<Boolean> delete(final String key) {
    return new SimpleResult<>(new Callable<Boolean>() {
      @Override
      public Boolean call() throws Exception {
        return storage.remove(keyValueKey(key)) != null;
      }
    });
  }

  byte[] keyValueKey(final String key) {
    return String.format(Locale.US, "k-v:%s", key).getBytes();
  }
}
