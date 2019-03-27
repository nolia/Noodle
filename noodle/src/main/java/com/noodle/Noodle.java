package com.noodle;

import android.content.Context;

import com.google.gson.Gson;
import com.noodle.collection.Collection;
import com.noodle.collection.Converter;
import com.noodle.collection.GsonConverter;
import com.noodle.collection.StoredConvertedCollection;
import com.noodle.storage.Encryption;
import com.noodle.storage.RandomAccessFileStorage;
import com.noodle.storage.Record;
import com.noodle.storage.Storage;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

  final Context context;
  Storage storage;

  final HashMap<String, Collection> collectionHashMap = new HashMap<>();
  final HashMap<String, Description> descriptionHashMap = new HashMap<>();
  final Converter converter;


  /**
   * Creates new builder instance
   *
   * @param context application context
   * @return new Builder instance
   */
  public static Builder with(final Context context) {
    return new Builder(context);
  }

  private static String defaultNoodleFile(final Context context) {
    return context.getFilesDir().getAbsolutePath() + File.separator + "data.noodle";
  }

  /**
   * Create new Noodle with default settings.
   *
   * @param context should probably be application context
   */
  public Noodle(final Context context) {
    this(context,
        new RandomAccessFileStorage(new File(defaultNoodleFile(context)), Encryption.NO_ENCRYPTION),
        new GsonConverter(new Gson())
    );
  }

  /**
   * Creates new Noodle with specified parameters.
   *
   * @param context   application context
   * @param storage   a storage object to use
   * @param converter which converter to use
   */
  public Noodle(final Context context, final Storage storage, final Converter converter) {
    this.context = context;
    this.storage = storage;
    this.converter = converter;
  }

  /**
   * Registers the type of objects which can be stored in this Noodle storage.
   * If there was previously registered type, rewrites its description with new one.
   *
   * @param description description of the type
   * @param <T>         generic type, to be statically type-safe
   * @return this Noodle instance
   */
  public <T> Noodle registerType(final Description<T> description) {
    descriptionHashMap.put(description.collectionName, description);
    return this;
  }

  /**
   * Returns the collection of the given type and name.<br/>
   * * <b>Note: type description have to be already registered with
   * * {@link #registerType(Description) }.
   *
   * @param type           type of objects to store
   * @param collectionName name of the collection
   * @param <T>            generic type, to be statically type-safe
   * @return collection of the given type.
   */
  @SuppressWarnings("unchecked")
  public <T> Collection<T> collectionOf(final Class<T> type, final String collectionName) {
    if (!descriptionHashMap.containsKey(collectionName)) {
      throw new IllegalArgumentException("Collection " + collectionName + " not found ");
    }
    Description<T> description = ((Description<T>) descriptionHashMap.get(collectionName));

    if (collectionHashMap.containsKey(collectionName)) {
      final Collection collection = collectionHashMap.get(collectionName);
      return ((Collection<T>) collection);
    }

    Collection<T> result = new StoredConvertedCollection<>(type, description, converter, storage);
    collectionHashMap.put(collectionName, result);

    return result;
  }

  /**
   * Returns the collection of the given type.<br/>
   * <b>Note: type description have to be already registered with
   * {@link #registerType(Description) }.
   *
   * @param type type of objects to store
   * @param <T>  generic type, to be statically type-safe
   * @return collection of the given type.
   */
  @SuppressWarnings("unchecked")
  public <T> Collection<T> collectionOf(final Class<T> type) {
    return collectionOf(type, type.getSimpleName());
  }

  /**
   * Get object from the storage.
   *
   * @param key  key, that object was previously stored with
   * @param type class of object to parse
   * @param <T>  type of the object
   * @return Call, wrapping wanted object, or null if not found
   */
  public <T> Call<T> get(final String key, final Class<T> type) {
    return new Call<>(new Callable<T>() {
      @Override
      public T call() {
        final byte[] keyBytes = keyValueKey(key);
        final Record record = storage.get(keyBytes);
        return record != null
            ? converter.fromBytes(record.getData(), type)
            : null;
      }
    });
  }

  /**
   * Put object to the storage.
   *
   * @param key   key that object will be stored with
   * @param value object
   * @param <T>   object type
   * @return Call, wrapping the same object
   */
  public <T> Call<T> put(final String key, final T value) {
    return new Call<>(new Callable<T>() {
      @Override
      public T call() {
        final byte[] keyBytes = keyValueKey(key);
        final byte[] data = converter.toBytes(value);
        storage.put(new Record(keyBytes, data));
        return value;
      }
    });
  }

  /**
   * Deletes object from storage
   *
   * @param key key to delete object by
   * @return Call, wrapping boolean, indicating whether object was deleted
   * by this operation
   */
  public Call<Boolean> delete(final String key) {
    return new Call<>(new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return storage.remove(keyValueKey(key)) != null;
      }
    });
  }

  byte[] keyValueKey(final String key) {
    return String.format(Locale.US, "k-v:%s", key).getBytes();
  }


  /**
   * Noodle Builder.
   */
  public static class Builder {

    private final Context context;
    private String filePath;
    private Converter converter;
    private Encryption encryption;

    final HashMap<String, Description> descriptionHashMap = new HashMap<>();

    public Builder(final Context context) {
      this.context = context;
      this.filePath = defaultNoodleFile(context);
    }

    /**
     * Sets the path to a noodle file
     *
     * @param filePath path to file
     * @return this builder instance
     */
    public Builder filePath(final String filePath) {
      this.filePath = filePath;
      return this;
    }

    /**
     * Sets the converter of Noodle to be built
     *
     * @param converter {@link Converter} to be used in this Noodle
     * @return this builder instance
     */
    public Builder converter(final Converter converter) {
      this.converter = converter;
      return this;
    }

    /**
     * Add given description and it's type to a set of registered types
     *
     * @param description description of type you want to store
     * @param <T>         type
     * @return this builder instance
     */
    public <T> Builder addType(final Description<T> description) {
      descriptionHashMap.put(description.collectionName, description);
      return this;
    }

    /**
     * Set the {@link Encryption} to be used in a result Noodle.
     *
     * @param encryption encryption to use
     * @return this builder instance
     */
    public Builder encryption(final Encryption encryption) {
      this.encryption = encryption;
      return this;
    }

    /**
     * Register given type to be used in storage with the specified collection name.
     * <b>NOTE: Entity class must have an annotated id field,
     * with {@link Id} annotation present.</b>
     *
     * @param type           type you want to store
     * @param collectionName name of the collection to use, must not be null or empty
     * @param <T>            type
     * @return this build instance
     */
    public <T> Builder addType(final Class<T> type, final String collectionName) {
      Field idField = null;
      for (Field field : type.getDeclaredFields()) {
        for (Annotation annotation : field.getAnnotations()) {
          if (annotation.annotationType() == Id.class) {
            if (idField != null) {
              throw new RuntimeException("Entity may have only one id field");
            }
            idField = field;
          }
        }
      }
      if (idField == null) {
        throw new RuntimeException("Entity must have an Id field");
      }

      return addType(
          Description.of(type)
              .withCollectionName(collectionName)
              .withIdField(idField.getName())
              .build()
      );
    }

    /**
     * Same as {@link #addType(Class, String)} but uses class {@link Class#getSimpleName() simple name}
     * as the name of the collection.
     *
     * @param type type you want to store
     * @param <T>  type
     * @return this build instance
     */
    public <T> Builder addType(final Class<T> type) {
      return addType(type, type.getSimpleName());
    }

    /**
     * Builds the Noodle according to params.
     *
     * @return Noodle instance
     */
    public Noodle build() {
      final Storage storage = new RandomAccessFileStorage(
          new File(filePath),
          encryption != null ? encryption : Encryption.NO_ENCRYPTION
      );
      final Noodle noodle = new Noodle(
          context,
          storage,
          converter != null ? converter : new GsonConverter(new Gson())
      );
      for (Description description : descriptionHashMap.values()) {
        noodle.registerType(description);
      }

      return noodle;
    }
  }
}
