package com.noodle.description;

import java.lang.reflect.Field;

/**
 * Knows how to get id for an item.
 */
public class Description<T> {


  private final Class<T> clazz;
  private final GetIdOperator<T> getIdOperator;
  private final SetIdOperator<T> setIdOperator;

  public Description(final Class<T> clazz,
                     final GetIdOperator<T> getIdOperator,
                     final SetIdOperator<T> setIdOperator) {
    this.clazz = clazz;
    this.getIdOperator = getIdOperator;
    this.setIdOperator = setIdOperator;
  }

  public static <T> DescriptionBuilder<T> of(Class<T> clazz) {
    return new DescriptionBuilder<>(clazz);
  }

  public long idOfItem(final T t) {
    return getIdOperator.getId(t);
  }

  public void setItemId(final T t, final long id) {
    setIdOperator.setId(t, id);
  }

  public static class DescriptionBuilder<T> {

    private final Class<T> clazz;
    private GetIdOperator<T> getIdOperator;
    private SetIdOperator<T> setIdOperator;

    public DescriptionBuilder(final Class<T> clazz) {
      this.clazz = clazz;
    }

    public DescriptionBuilder<T> withGetIdOperator(GetIdOperator<T> operator) {
      this.getIdOperator = operator;
      return this;
    }

    public DescriptionBuilder<T> withSetIdOperator(SetIdOperator<T> operator) {
      this.setIdOperator = operator;
      return this;
    }

    public  DescriptionBuilder<T> withIdField(String fieldName) {
      final ReflectionIdField<T> reflectionIdField = new ReflectionIdField<>(clazz, fieldName);
      this.getIdOperator = reflectionIdField;
      this.setIdOperator = reflectionIdField;
      return this;
    }

    public Description<T> build() {
      return new Description<>(clazz, getIdOperator, setIdOperator);
    }
  }

  public interface GetIdOperator<T> {
    long getId(T t);
  }

  public interface SetIdOperator<T> {
    void setId(T t, long id);
  }

  private static class ReflectionIdField<T> implements GetIdOperator<T>, SetIdOperator<T> {

    private Class<T> clazz;
    private Field field;

    public ReflectionIdField(final Class<T> clazz, final String fieldName) {
      this.clazz = clazz;
      try {
        field = clazz.getField(fieldName);

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
