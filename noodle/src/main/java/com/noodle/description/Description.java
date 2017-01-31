package com.noodle.description;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Knows how to get and set id of an item.
 */
public class Description<T> {

  final GetIdOperator<T> getIdOperator;
  final SetIdOperator<T> setIdOperator;

  public Description(final GetIdOperator<T> getIdOperator,
                     final SetIdOperator<T> setIdOperator) {
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
      return new Description<>(getIdOperator, setIdOperator);
    }
  }

  public interface GetIdOperator<T> {
    long getId(T t);
  }

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
