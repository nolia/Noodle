package com.noodle.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class RandomAccessFileStorage implements Storage {

  // Max size of the in-memory copy buffer. Currently 16 MB.
  private static final int MAX_COPY_BUFFER_SIZE = 16 * 1024 * 1024;
  private static final int MIN_COPY_BUFFER_SIZE = 1024;

  private final Encryption encryption;
  private final RandomAccessFile file;

  private final Object fileLock = new Object();
  private final Map<BytesWrapper, Long> index = new ConcurrentHashMap<>();

  public RandomAccessFileStorage(final File file, final Encryption encryption) {
    this.encryption = encryption;
    try {
      this.file = new RandomAccessFile(file, "rw");

      final boolean existed = file.exists();
      if (!existed) {
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
      } else {
        remapIndexes();
      }

    } catch (Exception e) {
      throw toRuntimeException(e);
    }
  }

  @Override
  public void put(final Record record) {
    synchronized (fileLock) {
      final byte[] key = record.key;
      final Record encryptedRecord = encryptRecord(record);

      final long existingPos = positionOf(key);

      if (existingPos != -1) {
        remove(key);
      }

      try {
        final long pos = file.length();

        file.seek(pos);
        file.writeInt(encryptedRecord.key.length);
        file.writeInt(encryptedRecord.data.length);
        file.write(encryptedRecord.key);
        file.write(encryptedRecord.data);

        index.put(new BytesWrapper(key), pos);
      } catch (IOException e) {
        throw toRuntimeException(e);
      }
    }
  }

  @Override
  public Record remove(final byte[] key) {
    synchronized (fileLock) {
      final long pos = positionOf(key);
      if (pos == -1) {
        return null;
      }

      final Record encryptedRecord = getRecordAt(pos);

      try {
        final int encryptedSize = encryptedRecord.size();

        // Last element.
        if (pos + encryptedSize >= file.length()) {
          file.setLength(pos);
        } else {
          // In the middle or beginning.

          final int remainderSize = (int) (file.length() - pos);
          byte[] copyBuffer = allocateCopyBuffer(remainderSize);

          long fromPos = pos + encryptedSize;
          long toPos = pos;
          long fileLength = file.length();

          while (fromPos < fileLength) {
            file.seek(fromPos);
            int read = file.read(copyBuffer);
            fromPos += read;

            file.seek(toPos);
            file.write(copyBuffer, 0, read);

            toPos += read;
          }

          file.setLength(fileLength - encryptedSize);

          // Update indexes.
          for (BytesWrapper wrapper : index.keySet()) {
            final long recordPos = index.get(wrapper);

            if (recordPos > pos) {
              index.put(wrapper, recordPos - encryptedSize);
            }
          }
        }

        index.remove(new BytesWrapper(key));

        return decryptRecord(encryptedRecord);
      } catch (IOException e) {
        throw toRuntimeException(e);
      }
    }
  }

  private byte[] allocateCopyBuffer(final int remainderSize) {
    final Runtime runtime = Runtime.getRuntime();
    final int usedMemory = (int) (runtime.totalMemory() - runtime.freeMemory());
    final int availableMemory = (int) (runtime.maxMemory() - usedMemory);

    // Set buffer size to be as much as remaining size of the file,
    // but not bigger than a half of available heap and not bigger
    // than MAX_COPY_BUFFER_SIZE limit.
    int size = Math.min(remainderSize, availableMemory / 2);
    size = Math.min(size, MAX_COPY_BUFFER_SIZE);
    size = Math.max(size, MIN_COPY_BUFFER_SIZE);

    return new byte[size];
  }

  @Override
  public Record get(final byte[] key) {
    synchronized (fileLock) {
      final long pos = positionOf(key);

      return pos != -1
          ? decryptRecord(getRecordAt(pos))
          : null;
    }
  }

  private Record encryptRecord(final Record original) {
    try {
      return new Record(
          original.key,
          encryption.encrypt(original.key, original.data)
      );
    } catch (Exception e) {
      throw toRuntimeException(e);
    }
  }

  @Override
  public List<byte[]> prefixedWith(final byte[] prefix) {
    final ArrayList<byte[]> keys = new ArrayList<>();
    for (BytesWrapper wrapper : index.keySet()) {
      if (wrapper.hasPrefix(prefix)) {
        keys.add(wrapper.bytes);
      }
    }
    return Collections.unmodifiableList(keys);
  }

  public Map<BytesWrapper, Long> getIndex() {
    return index;
  }

  private Record decryptRecord(final Record encrypted) {
    try {
      return new Record(
          encrypted.key,
          encryption.decrypt(encrypted.key, encrypted.data)
      );
    } catch (Exception e) {
      throw toRuntimeException(e);
    }
  }

  private Record getRecordAt(final long position) {
    try {
      file.seek(position);

      final int keySize = file.readInt();
      final int dataSize = file.readInt();

      final byte[] keyBytes = new byte[keySize];
      final byte[] dataBytes = new byte[dataSize];

      file.read(keyBytes);
      file.read(dataBytes);

      return new Record(keyBytes, dataBytes);

    } catch (IOException e) {
      throw toRuntimeException(e);
    }
  }

  private long positionOf(final byte[] key) {
    final BytesWrapper keyWrapper = new BytesWrapper(key);
    final Long pos = index.get(keyWrapper);
    return pos == null ? -1 : pos;
  }

  private void remapIndexes() throws Exception {
    synchronized (fileLock) {
      file.seek(0);

      while (file.getFilePointer() < file.length()) {
        final long pos = file.getFilePointer();
        final int keySize = file.readInt();

        if (keySize == 0) {
          break;
        }

        final int dataSize = file.readInt();
        final byte[] key = new byte[keySize];

        // Read key and check if all read.
        if (file.read(key) != keySize) {
          throw new RuntimeException("Data is corrupted at " + file.getFilePointer());
        }

        index.put(new BytesWrapper(key), pos);

        file.seek(file.getFilePointer() + dataSize);
      }
    }
  }

  /**
   * Converts checked exception to runtime exception so that no mandatory try-catch is required.
   */
  private RuntimeException toRuntimeException(Exception e) {
    return new RuntimeException(e);
  }
}
