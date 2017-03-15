package com.noodle.storage;

import java.util.Arrays;

/**
 * Wraps a byte[] array, adding to it {@link Comparable} and {@link Object#hashCode()} and
 * {@link Object#equals(Object)} implementations. <br/>
 * Primary is used by {@link Storage} to serve as byte array keys in index maps.
 */
public class BytesWrapper implements Comparable<BytesWrapper> {

  final byte[] bytes;

  public BytesWrapper(final byte[] bytes) {
    this.bytes = bytes;
  }

  public byte[] getBytes() {
    return bytes;
  }

  @Override
  public int compareTo(final BytesWrapper another) {
    if (another == null) {
      return bytes[0];
    }

    final int minLength = bytes.length < another.bytes.length
        ? bytes.length
        : another.bytes.length;

    byte thisByte, otherByte;
    for (int i = 0; i < minLength; i++) {
      thisByte = bytes[i];
      otherByte = another.bytes[i];

      if (thisByte == otherByte) {
        continue;
      }

      return thisByte - otherByte;
    }

    return bytes.length - another.bytes.length;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BytesWrapper that = (BytesWrapper) o;

    return Arrays.equals(bytes, that.bytes);

  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  public boolean hasPrefix(final byte[] prefix) {
    if (prefix.length > bytes.length) {
      return false;
    }

    for (int i = 0; i < prefix.length; i++) {
      if (bytes[i] != prefix[i]) {
        return false;
      }
    }

    return true;
  }

  @Override
  public String toString() {
    return new String(bytes);
  }
}
