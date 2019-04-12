package com.noodle.storage

import java.lang.IllegalArgumentException
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.ArrayList

class DataCollection(
        private val storage: Storage,
        private val name: String
) {

    private val sequenceId: AtomicLong
    private val lock = Any()
    private val idSet = ArrayList<Long>()

    init {
        val maxId = storage
                .getKeysWithPrefix(name.toByteArray())
                .asSequence()
                .map { bytes -> bytes.toId()?.also { idSet += it } }
                .filterNotNull()
                .max() ?: 0

        sequenceId = AtomicLong(Math.max(maxId, 0))
    }

    fun size(): Int = idSet.size

    fun nextId(): Long = sequenceId.incrementAndGet()

    fun get(id: Long): ByteArray? = synchronized(lock) {
        storage.get(entityKey(id))?.data
    }

    fun put(id: Long, data: ByteArray) = synchronized(lock) {
        storage.put(Record(entityKey(id), data))
        idSet += id
    }

    fun delete(id: Long): ByteArray? = synchronized(lock) {
        val removed = storage.remove(entityKey(id)) ?: return null
        idSet -= id
        return removed.data
    }

    fun dataIterator(): DataIterator = DataIterator()

    private fun entityKey(id: Long): ByteArray = "$name:$id".toByteArray()

    private fun ByteArray.toId(): Long? {
        val separatorIndex = indexOf(':'.toByte())
        return when (separatorIndex) {
            in 1..(size - 1) -> String(sliceArray((separatorIndex + 1)..(size - 1))).toLongOrNull()
            else -> null
        }
    }

    inner class DataIterator : MutableIterator<ByteArray> {
        private var currentIndex = -1

        override fun hasNext(): Boolean {
            synchronized(lock) {
                return idSet.size > currentIndex + 1
            }
        }

        override fun next(): ByteArray {
            synchronized(lock) {
                val newIndex = currentIndex + 1
                val item = get(idSet[newIndex]) ?: throw IllegalArgumentException("Invalid id. Item not found!")
                currentIndex = newIndex
                return item
            }
        }

        override fun remove() {
            synchronized(lock) {
                delete(idSet[currentIndex])
            }
        }
    }
}
