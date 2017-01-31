package com.noodle.util;

/**
 *
 */
public class Data {

  // Used in tests.
  @SuppressWarnings("unused")
  public final long finalId = 0;

  public long id;

  public String name;

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Data data = (Data) o;

    if (id != data.id) return false;
    return name != null ? name.equals(data.name) : data.name == null;

  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (name != null ? name.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Data{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
