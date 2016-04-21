package com.noodle

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.util.*

/**
 * @author Nikolay Soroka - Stanfy (http://stanfy.com)
 */
class Storage(val file: File) {

  private val raf: RandomAccessFile
  private val channel: FileChannel

  internal val index = TreeMapIndex()

  private var _buffer: ByteArray? = null
  private val copyBuffer: ByteArray
    get() {
      if (_buffer == null) {
        _buffer = ByteArray(1024 * 1024)
      }
      return _buffer ?: throw IllegalStateException("Copy buffer is null!")
    }

  constructor(name: String) : this(File(name))

  init {
    raf = RandomAccessFile(file, "rw")
    channel = raf.channel

    if (file.exists()) {
      buildIndex()
    } else {
      val created = file.createNewFile()
      if (!created) {
        throw IllegalArgumentException("Could not create file at specified path: " + file.absolutePath)
      }
    }
  }

  fun close() {
    channel.close()
    raf.close()
  }

  private fun buildIndex() {
    if (raf.length() == 0L) {
      return
    }

    // FIXME use dynamic buffer size - data may be bigger than 1kb
    val buffer = ByteArray(1024)
    var read = 0
    var keySize: Int
    var valueSize: Int
    var pointer = 0L

    while (read != -1) {
      try {
        raf.seek(pointer)

        keySize = raf.readInt()
        valueSize = raf.readInt()

        read = raf.read(buffer, 0, keySize)
        if (read != keySize) {
          break;
        }
        val keyBytes = Arrays.copyOfRange(buffer, 0, keySize)
        val key = String(keyBytes)
        index.put(key, pointer)

        pointer = raf.filePointer + valueSize

      } catch (e: IOException) {
        break
      }
    }
  }


  // TODO implement
  fun get(encodedKey: String): Record? {
    if (!index.containsKey(encodedKey)) {
      return null
    }

    val pointer = index.get(encodedKey)!!

    raf.seek(pointer)
    val keySize = raf.readInt()
    val valueSize = raf.readInt()

    val keyBytes = ByteArray(keySize)
    raf.read(keyBytes)

    val data = ByteArray(valueSize)
    raf.read(data)

    val key = String(keyBytes)

    return Record(key, data)
  }

  fun put(record: Record) {
    if (index.containsKey(record.encodedKey)) {
      writeInTheMiddle(record)
      return
    }

    append(record)
  }

  private fun writeInTheMiddle(record: Record) {
    val pointer = index.get(record.encodedKey)
    if (pointer == null) {
      // Safe call.
      append(record)
      return
    }

    try {
      raf.seek(pointer)
      val keySize = raf.readInt()
      val dataSize = raf.readInt()

      val newSize = record.data.size
      // We have the same amount of space.
      if (newSize == dataSize) {
        raf.skipBytes(keySize)
        raf.write(record.data)
        return
      }

      // New data is smaller then already in the file.
      if (newSize < dataSize) {
        writeRecordData(pointer, record)

        if (index.isPointerLast(pointer)) {
          // Just trim.
          raf.setLength(raf.filePointer)
          return
        }

        val nextPointer = index.nextPointer(pointer)!!
        val diff = nextPointer - raf.filePointer
        shiftContentLeft(nextPointer, diff)
      } else {
        // New data is bigger - need to reallocate file.
        if (index.isPointerLast(pointer)) {
          writeRecordData(pointer, record)
          return
        }

        val next = index.nextPointer(pointer)!!
        val diff = next - pointer
        shiftContentLeft(next, diff)

        append(record)

      }

    } catch (e: IOException) {
      // Could not write to file.
    }

  }

  private fun writeRecordData(pointer: Long, record: Record) {
    raf.seek(pointer)
    raf.readInt() // Skip the key size.
    raf.write(record.data.size)
    raf.skipBytes(record.encodedKey.length)
    raf.write(record.data)
  }

  @Throws(IOException::class)
  private fun shiftContentLeft(pointer: Long, diff: Long) {
    var to = pointer - diff
    var from = pointer

    var read = 0
    do {
      raf.seek(from)
      read = raf.read(copyBuffer)
      if (read == -1) {
        break
      }

      from = raf.filePointer

      raf.seek(to)
      raf.write(copyBuffer, 0, read)

      to = raf.filePointer

    } while (from < raf.length())

    index.shiftPointersLeft(pointer, diff)

    raf.setLength(raf.length() - diff)
  }

  private fun append(record: Record) {
    val key = record.encodedKey
    val data = record.data

    val pointer = raf.length()
    raf.seek(pointer)
    raf.writeInt(key.length)
    raf.writeInt(data.size)

    raf.write(key.toByteArray())
    raf.write(data)

    index.put(key, pointer)
  }

  fun delete(key: String): Boolean {
    if (!index.containsKey(key)) {
      return false
    }

    val pointer = index.get(key)!!

    if (index.isPointerLast(pointer)) {
      // Just trim last.
      raf.setLength(pointer)
    } else {
      val next = index.nextPointer(pointer)!!
      val diff = next - pointer

      shiftContentLeft(next, diff)
    }

    index.remove(key)

    return true
  }

  interface Index {
    fun put(key: String, pointer: Long)

    fun get(key: String): Long?

    fun nextPointer(pointer: Long): Long?

    fun containsKey(key: String): Boolean

    fun isPointerLast(pointer: Long): Boolean

    fun shiftPointersLeft(from: Long, diff: Long)

    fun remove(key: String)

  }

  class TreeMapIndex: Index {

    private val indexMap: TreeMap<String, Long> = TreeMap()

    private val pointers: ArrayList<Long> = ArrayList()

    override fun containsKey(key: String): Boolean = indexMap.containsKey(key)

    override fun put(key: String, pointer: Long) {
      indexMap.put(key, pointer)
      pointers.add(pointer)
      Collections.sort(pointers)
    }

    override fun get(key: String): Long? = indexMap[key]

    override fun nextPointer(pointer: Long): Long? {
      val index = pointers.indexOf(pointer)
      if (index == -1 || index == pointers.size - 1) {
        return null
      }

      return pointers[index + 1]
    }

    override fun isPointerLast(pointer: Long): Boolean = pointers.last() == pointer

    override fun shiftPointersLeft(from: Long, diff: Long) {
      val start = pointers.indexOf(from)
      if (start == -1) {
        return
      }

      var i = start
      while (i < pointers.size) {
        val old = pointers[i]
        val newValue = old - diff

        val entry = indexMap.entries.find { e -> e.value == old }
        if (entry != null) {
          indexMap[entry.key] = newValue
          pointers[i] = newValue
        }

        i++
      }
    }

    override fun remove(key: String) {
      val pointer = indexMap[key] ?: return
      indexMap.remove(key)
      pointers.remove(pointer)
      Collections.sort(pointers)
    }

  }
}

