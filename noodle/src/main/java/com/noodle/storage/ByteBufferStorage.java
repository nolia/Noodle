package com.noodle.storage;

import com.noodle.encryption.Encryption;
import com.noodle.encryption.NoEncryption;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Storage, backed by ByteBuffer.
 */
public class ByteBufferStorage implements Storage {

  static final int INITIAL_SIZE = 128;

  private final Encryption encryption;

  public ByteBufferStorage() {
    this(new NoEncryption());
  }

  public ByteBufferStorage(final Encryption encryption) {
    this.encryption = encryption;
  }

  ByteBuffer buffer = ByteBuffer.allocate(INITIAL_SIZE);

  TreeMap<BytesWrapper, Integer> treeMapIndex = new TreeMap<>();
  int lastPosition = 0;

  @Override
  public synchronized void put(final Record record) {
    final byte[] key = record.key;
    final Record encryptedRecord = encryptRecord(record);

    if (encryptedRecord.size() + lastPosition >= buffer.limit()) {
      // Grow the buffer.
      growBufferSize(buffer.limit() + 2 * encryptedRecord.size());
    }

    // Check if need to replace.
    final int existingPosition = positionOf(key);
    if (existingPosition >= 0) {
      final Record savedRecord = getRecordAt(existingPosition);

      if (encryptedRecord.data.length == savedRecord.data.length) {
        // Put on the same place, don't update indexes.
        buffer.put(encryptedRecord.asByteBuffer());

        lastPosition = buffer.position();
        return;
      }

      remove(key);
    }

    // Append the new record.
    buffer.position(lastPosition);
    buffer.put(encryptedRecord.asByteBuffer());
    treeMapIndex.put(new BytesWrapper(key), lastPosition);

    lastPosition = buffer.position();
  }

  private Record encryptRecord(final Record record) {
    try {
      return new Record(
          encryption.encrypt(record.key),
          encryption.encrypt(record.data)
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Record decryptRecord(final Record record) {
    try {
      return new Record(
          encryption.decrypt(record.key),
          encryption.decrypt(record.data)
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  protected void growBufferSize(final int newSize) {
    final ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
    newBuffer.put(buffer);

    buffer = newBuffer;
  }

  @Override
  public synchronized Record remove(final byte[] key) {
    final int pos = positionOf(key);

    if (pos == -1) {
      return null;
    }

    final Record record = getRecordAt(pos);

    if (treeMapIndex.size() == 1) {
      lastPosition = 0;
      buffer.position(0);
      buffer.put(new byte[record.size()]);
      buffer.position(0);
      treeMapIndex.remove(new BytesWrapper(key));
      return record;
    }

    int removedBytes;
    if (pos == 0) {
      // First element, use compact.
      buffer.compact();

      removedBytes = record.size();
    } else if (buffer.position() == lastPosition) {
      // Check if last element.
      lastPosition = pos;

      removedBytes = record.size();
    } else {
      // Element in the middle, need to compact.

      final byte[] temp = new byte[buffer.remaining()];
      buffer.get(temp);

      buffer.position(pos);
      buffer.put(temp);

      removedBytes = record.size();

    }
    lastPosition -= removedBytes;

    buffer.position(lastPosition);

    treeMapIndex.remove(new BytesWrapper(key));

    if (removedBytes > 0) {
      for (BytesWrapper keyInIndex : treeMapIndex.keySet()) {
        final int position = treeMapIndex.get(keyInIndex);

        if (position > pos) {
          treeMapIndex.put(keyInIndex, position - removedBytes);
        }
      }

      final int newSize = buffer.limit() - removedBytes;
      shrinkBuffer(newSize);
    }

    return record;
  }

  protected void shrinkBuffer(final int newSize) {
    final ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
    newBuffer.put(buffer.array(), 0, newSize);
    buffer = newBuffer;
  }

  @Override
  public synchronized Record get(final byte[] key) {
    final int pos = positionOf(key);

    return pos != -1
        ? decryptRecord(getRecordAt(pos))
        : null;
  }

  @Override
  public synchronized List<byte[]> prefixedWith(final byte[] prefix) {
    final ArrayList<byte[]> keys = new ArrayList<>();
    for (BytesWrapper wrapper : treeMapIndex.keySet()) {
      if (wrapper.hasPrefix(prefix)) {
        keys.add(wrapper.bytes);
      }
    }
    return Collections.unmodifiableList(keys);
  }

  public Map<BytesWrapper, Integer> getTreeMapIndex() {
    return Collections.unmodifiableSortedMap(treeMapIndex);
  }

  private Record getRecordAt(final int position) {
    buffer.position(position);
    final int keyLength = buffer.getInt();
    final int dataLength = buffer.getInt();

    final byte[] keyBytes = new byte[keyLength];
    final byte[] dataBytes = new byte[dataLength];

    buffer.get(keyBytes)
        .get(dataBytes);

    return new Record(keyBytes, dataBytes);
  }

  private int positionOf(final byte[] key) {
    final Integer pos = treeMapIndex.get(new BytesWrapper(key));
    return pos == null ? -1 : pos;
  }
}
