package com.noodle.storage;

import java.nio.ByteBuffer;
import java.util.TreeMap;

/**
 *
 */

public class ByteBufferStorage implements Storage {

  // 4 kb
  private static final int INITIAL_SIZE = 4 * 1024;

  ByteBuffer buffer = ByteBuffer.allocate(INITIAL_SIZE);

  TreeMap<BytesWrapper, Integer> treeMapIndex = new TreeMap<>();
  int lastPosition = 0;

  @Override
  public void put(final Record record) {
    // Check if need to replace.
    final int existingPosition = positionOf(record.key);
    if (existingPosition >= 0) {
      final Record savedRecord = getRecordAt(existingPosition);

      if (record.data.length == savedRecord.data.length) {
        // Put on the same place, don't update indexes.
        final int dataStart = 8 + record.key.length;
        buffer.position(dataStart);
        buffer.put(record.data);

        lastPosition = buffer.position();
        return;
      }

      remove(record.key);
    }

    // New record - append.
    if (record.size() + lastPosition >= buffer.limit()) {
      // Grow the buffer.
      final ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
      newBuffer.put(buffer);

      buffer = newBuffer;
    }

    buffer.position(lastPosition);
    buffer.put(record.asByteBuffer());
    treeMapIndex.put(new BytesWrapper(record.key), lastPosition);

    lastPosition = buffer.position();
  }


  @Override
  public Record remove(final byte[] key) {
    final int pos = positionOf(key);

    if (pos == -1) {
      return null;
    }

    final Record record = getRecordAt(pos);

    if (treeMapIndex.size() == 1) {
      lastPosition = 0;
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

      removedBytes = 0;
    } else {
      // Element in the middle, need to compact.

      final byte[] temp = new byte[buffer.remaining()];
      buffer.get(temp);

      buffer.position(pos);
      buffer.put(temp);

      removedBytes = record.size();

      lastPosition -= removedBytes;
    }

    buffer.position(lastPosition);

    treeMapIndex.remove(new BytesWrapper(key));

    if (removedBytes > 0) {
      for (BytesWrapper keyInIndex : treeMapIndex.keySet()) {
        final int position = treeMapIndex.get(keyInIndex);

        if (position > pos) {
          treeMapIndex.put(keyInIndex, position - removedBytes);
        }
      }
    }

    return record;
  }

  @Override
  public Record get(final byte[] key) {
    final int pos = positionOf(key);

    return pos != -1
        ? getRecordAt(pos)
        : null;
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
