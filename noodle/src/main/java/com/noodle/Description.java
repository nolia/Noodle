package com.noodle;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Knows how to get and set id of an item.<br/>
 * To construct, use builder with {@link #of(Class)} method.
 */
public class Description<T> {

  final Class<T> clazz;
  final String collectionName;
  final GetIdOperator<T> getIdOperator;
  final SetIdOperator<T> setIdOperator;

  private Description(final Class<T> clazz,
                      final String collectionName,
                      final GetIdOperator<T> getIdOperator,
                      final SetIdOperator<T> setIdOperator) {
    this.clazz = clazz;
    this.collectionName = collectionName;
    this.getIdOperator = getIdOperator;
    this.setIdOperator = setIdOperator;
  }

  /**
   * Creates new {@link DescriptionBuilder} to create description of the item.
   *
   * @param clazz type of entities
   * @param <T>   type of entities
   * @return new {@link DescriptionBuilder}
   */
  public static <T> DescriptionBuilder<T> of(Class<T> clazz) {
    return new DescriptionBuilder<>(clazz);
  }

  /**
   * Uses {@link GetIdOperator} to get id of the item.
   *
   * @param t item to get id of
   * @return item id, or 0 if not present.
   */
  public long idOfItem(final T t) {
    return getIdOperator.getId(t);
  }

  /**
   * Uses {@link SetIdOperator} to set id of the item.
   *
   * @param t  entity
   * @param id its id
   */
  public T setItemId(final T t, final long id) {
    return setIdOperator.setId(t, id);
  }

  /**
   * Type this is description of
   *
   * @return this is description of
   */
  public Class<T> getType() {
    return clazz;
  }

  /**
   * Returns the name of this collection.
   *
   * @return the name of this collection
   */
  public String getCollectionName() {
    return collectionName;
  }

  /**
   * Helper class to build Descriptions.
   *
   * @param <T> entity type
   */
  public static class DescriptionBuilder<T> {

    private final Class<T> clazz;
    private String collectionName;
    private GetIdOperator<T> getIdOperator;
    private SetIdOperator<T> setIdOperator;

    DescriptionBuilder(final Class<T> clazz) {
      this.clazz = clazz;
      this.collectionName = clazz.getSimpleName();
    }

    /**
     * Changes getIdOperator.
     *
     * @param operator new {@link GetIdOperator} to use
     * @return this builder instance
     */
    public DescriptionBuilder<T> withGetIdOperator(GetIdOperator<T> operator) {
      this.getIdOperator = operator;
      return this;
    }

    /**
     * Changes setIdOperator.
     *
     * @param operator new {@link SetIdOperator} to use
     * @return this builder instance
     */
    public DescriptionBuilder<T> withSetIdOperator(SetIdOperator<T> operator) {
      this.setIdOperator = operator;
      return this;
    }

    /**
     * Sets getId and setId operators to use reflection to access id of an entity.<br>
     * <b>Note: id field must be of the type Long or long and may not be final.</b>
     *
     * @param fieldName name of the id field
     * @return this builder instance
     */
    public DescriptionBuilder<T> withIdField(String fieldName) {
      final ReflectionIdField<T> reflectionIdField = new ReflectionIdField<>(clazz, fieldName);
      this.getIdOperator = reflectionIdField;
      this.setIdOperator = reflectionIdField;
      return this;
    }

    /**
     * Overrides the collection name. Useful when migrating data.
     * By default collection name is set to {@link Class#getSimpleName()}.
     *
     * @param collectionName collection name to be used.
     * @return this builder instance
     */
    public DescriptionBuilder<T> withCollectionName(final String collectionName) {
      this.collectionName = collectionName;
      return this;
    }

    /**
     * Creates new {@link Description} object, based on this item.
     * Note, that getId and setId operators must be presetn.
     *
     * @return new description based on this builder
     */
    public Description<T> build() {
      if (getIdOperator == null) {
        throw new RuntimeException("Get id operator may not be null");
      }

      if (setIdOperator == null) {
        throw new RuntimeException("Set id operator may not be null");
      }

      if (collectionName == null || collectionName.isEmpty()) {
        throw new RuntimeException("Collection name may not be empty or null. Found " + collectionName);
      }

      return new Description<>(clazz, collectionName, getIdOperator, setIdOperator);
    }
  }

  /**
   * Function to get the id of item
   *
   * @param <T> item type
   */
  public interface GetIdOperator<T> {
    long getId(T t);
  }

  /**
   * Function to set the id of item
   *
   * @param <T> item type
   */
  public interface SetIdOperator<T> {
    T setId(T t, long id);
  }

  static class ReflectionIdField<T> implements GetIdOperator<T>, SetIdOperator<T> {

    private Field field;

    ReflectionIdField(final Class<T> clazz, final String fieldName) {
      try {
        field = clazz.getDeclaredField(fieldName);

        if (Modifier.isFinal(field.getModifiers())) {
          throw new RuntimeException("id field cannot be final!");
        }

        if (field.getType() != Long.TYPE && field.getType() != Long.class) {
          throw new RuntimeException("Field type should be long or Long");
        }

        if (!field.isAccessible()) {
          field.setAccessible(true);
        }
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public long getId(final T t) {
      try {
        return field.getLong(t);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public T setId(final T t, final long id) {
      try {
        field.setLong(t, id);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      return t;
    }
  }
}
