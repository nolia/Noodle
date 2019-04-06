package com.noodle.storage

import java.util.*

class Record(
        val key: ByteArray,
        val data: ByteArray
) {

    val size: Long = 8L + key.size + data.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Record

        if (size != other.size) return false
        if (!key.contentEquals(other.key)) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = key.contentHashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + size.hashCode()
        return result
    }

    override fun toString(): String {
        return "Record(key=${Arrays.toString(key)}, data=${Arrays.toString(data)}, size=$size)"
    }


}

interface Storage {

    fun get(key: ByteArray): Record?

    fun put(record: Record)

    fun remove(key: ByteArray): Record?

    fun getKeys(): List<ByteArray>

    fun getKeysWithPrefix(prefix: ByteArray): List<ByteArray>
}
