package com.noodle;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Knows how to get and set id of an item.<br/>
 * To construct, use builder with {@link #of(Class)} method.
 */
public class Description<T> {

  final Class<T> clazz;
  final GetIdOperator<T> getIdOperator;
  final SetIdOperator<T> setIdOperator;

  public Description(final Class<T> clazz, final GetIdOperator<T> getIdOperator,
                     final SetIdOperator<T> setIdOperator) {
    this.clazz = clazz;
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
   * @param t entity
   * @param id its id
   */
  public void setItemId(final T t, final long id) {
    setIdOperator.setId(t, id);
  }

  /**
   * Type this is description of
   * @return this is description of
   */
  public Class<T> getType() {
    return clazz;
  }

  /**
   * Helper class to build Descriptions.
   *
   * @param <T> entity type
   */
  public static class DescriptionBuilder<T> {

    private final Class<T> clazz;
    private GetIdOperator<T> getIdOperator;
    private SetIdOperator<T> setIdOperator;

    public DescriptionBuilder(final Class<T> clazz) {
      this.clazz = clazz;
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
    public  DescriptionBuilder<T> withIdField(String fieldName) {
      final ReflectionIdField<T> reflectionIdField = new ReflectionIdField<>(clazz, fieldName);
      this.getIdOperator = reflectionIdField;
      this.setIdOperator = reflectionIdField;
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

      return new Description<>(clazz, getIdOperator, setIdOperator);
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
    void setId(T t, long id);
  }

  static class ReflectionIdField<T> implements GetIdOperator<T>, SetIdOperator<T> {

    private Field field;

    public ReflectionIdField(final Class<T> clazz, final String fieldName) {
      try {
        field = clazz.getField(fieldName);

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
    public void setId(final T t, final long id) {
      try {
        field.setLong(t, id);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
