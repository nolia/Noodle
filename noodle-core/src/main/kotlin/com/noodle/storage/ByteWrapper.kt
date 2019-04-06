@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.noodle.storage

inline class ByteWrapper(val bytes: ByteArray) : Comparable<ByteWrapper> {

    val size: Int get() = bytes.size

    override fun compareTo(other: ByteWrapper): Int {
        val minLength = if (size < other.size) size else other.size

        for (i in 0 until minLength) {
            val thisByte = bytes[i]
            val otherByte = other.bytes[i]

            if (thisByte != otherByte) return thisByte - otherByte
        }

        return size - other.size
    }

    fun hasPrefix(prefix: ByteArray): Boolean {
        if (prefix.size > size) return false

        for (i in 0 until prefix.size) {
            if (bytes[i] != prefix[i]) return false
        }

        return true
    }
}

