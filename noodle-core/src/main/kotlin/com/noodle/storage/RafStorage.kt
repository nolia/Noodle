package com.noodle.storage

import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private const val MAX_COPY_BUFFER_SIZE = 16 * 1024 * 1024 // 16 Mb.
private const val MIN_COPY_BUFFER_SIZE = 1024 // 1 Kb.

private const val NO_POSITION = -1L

/**
 * Primary storage implementation with [RandomAccessFile].
 */
class RafStorage(file: File) : Storage {

    private val file: RandomAccessFile = RandomAccessFile(file, "rw")
    private val fileLock = ReentrantReadWriteLock()
    private val index = ConcurrentHashMap<ByteWrapper, Long>()

    init {
        val existed = file.exists()
        if (!existed) {
            file.createNewFile()
        } else {
            remapIndexes()
        }
    }


    //region Storage

    override fun get(key: ByteArray): Record? {
        fileLock.read {
            val pos = positionOf(key)
            return if (pos != NO_POSITION) getRecordAt(pos) else null
        }
    }


    override fun put(record: Record) {
        fileLock.write {
            val key = record.key

            val previousPos = positionOf(key)
            if (previousPos != NO_POSITION) {
                // TODO Try to rewrite at the same place.
                removeImpl(key)
            }

            val pos = file.length()
            file.seek(pos)
            file.writeInt(record.key.size)
            file.writeInt(record.data.size)
            file.write(record.key)
            file.write(record.data)

            index[ByteWrapper(key)] = pos
        }
    }

    override fun remove(key: ByteArray): Record? {
        fileLock.write {
            return removeImpl(key)
        }
    }

    override fun getKeys(): List<ByteArray> = index
            .keys
            .map { it.bytes }
            .toList()

    override fun getKeysWithPrefix(prefix: ByteArray): List<ByteArray> = index
            .keys
            .asSequence()
            .filter { it.hasPrefix(prefix) }
            .map { it.bytes }
            .toList()

    //endregion

    //region Implementation

    private fun removeImpl(key: ByteArray): Record? {
        val pos = positionOf(key)
        if (pos == NO_POSITION) return null

        val record = getRecordAt(pos)
        val recordSize = record.size

        // If this record is the last one in file - just trim the file size.
        if (pos + recordSize >= file.length()) {
            file.setLength(pos)
            return record
        }

        val remainingSize = file.length() - pos
        val copyBuffer = allocateCopyBuffer(remainingSize)

        var fromPos = pos + recordSize
        var toPos = pos

        while (fromPos < file.length()) {
            file.seek(fromPos)
            val readSize = file.read(copyBuffer)
            fromPos += readSize

            file.seek(toPos)
            file.write(copyBuffer, 0, readSize)

            toPos += readSize
        }

        file.setLength(file.length() - recordSize)

        // Shift the indexes.
        for (wrapper in index.keys()) {
            // TODO Potential improvement: Iterate only on the affected values.
            val recordPos = index[wrapper] ?: continue

            if (recordPos > pos) {
                index[wrapper] = recordPos - recordSize
            }
        }

        return record
    }


    private fun positionOf(key: ByteArray): Long = index[ByteWrapper(key)] ?: -1

    private fun getRecordAt(pos: Long): Record {
        file.seek(pos)

        val keySize = file.readInt()
        val dataSize = file.readInt()

        val key = ByteArray(keySize).also { file.read(it) }
        val data = ByteArray(dataSize).also { file.read(it) }

        return Record(key, data)
    }

    private fun remapIndexes() {
        fileLock.write {
            file.seek(0)

            while (file.filePointer < file.length()) {
                val pos = file.filePointer

                // Read key size.
                val keySize = file.readInt()
                if (keySize == 0) break
                // Read data size.
                val dataSize = file.readInt()

                // Read key itself and put it into index.
                val key = ByteArray(keySize)
                if (file.read(key) != keySize) {
                    throw RuntimeException("Data is corrupted at $pos")
                }
                index[ByteWrapper(key)] = pos

                // Move to the next.
                file.seek(file.filePointer + dataSize)
            }
        }
    }

    private fun allocateCopyBuffer(remainingSize: Long): ByteArray {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val availableMemory = runtime.maxMemory() - usedMemory

        // Set buffer size to be as much as remaining size of the file,
        // but not bigger than a half of available heap and not bigger
        // than MAX_COPY_BUFFER_SIZE limit.

        var size = Math.min(remainingSize, availableMemory / 2)
        size = Math.min(size, MAX_COPY_BUFFER_SIZE.toLong())
        size = Math.max(size, MIN_COPY_BUFFER_SIZE.toLong())

        return ByteArray(size.toInt())
    }


    //endregion
}
