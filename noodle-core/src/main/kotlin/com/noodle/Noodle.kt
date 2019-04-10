package com.noodle

import com.noodle.storage.RafStorage
import com.noodle.storage.Record
import com.noodle.storage.Storage
import java.io.File

internal const val NOODLE_KEY_PREFIX = "\$noodle\$"

class Noodle internal constructor(
        private val storage: Storage,
        private val converter: Converter
) {

    //region Public Interface

    fun <T> get(key: String, clazz: Class<T>): T? {
        val record = storage.get(recordKey(key)) ?: return null
        return converter.fromBytes(record.data, clazz)
    }


    inline operator fun <reified T> get(key: String): T? =
            get(key, T::class.java)

    inline operator fun <reified T> get(vararg keys: String): List<T?> =
            keys.map { get(it, T::class.java) }

    inline operator fun <reified T> set(key: String, value: T?) {
        if (value == null) {
            delete(key)
        } else {
            put(key, value)
        }
    }

    fun <T> put(key: String, value: T) {
        val record = Record(
                key = recordKey(key),
                data = converter.toBytes(value)
        )
        storage.put(record)
    }

    fun delete(key: String): Boolean = storage.remove(recordKey(key)) != null

    //endregion

    private fun recordKey(key: String): ByteArray {
        val noodleKey = "$NOODLE_KEY_PREFIX:$key"
        return converter.toBytes(noodleKey)
    }

    class Builder {
        lateinit var storage: Storage
        lateinit var converter: Converter

        fun setFilePath(filePath: String) {
            storage = RafStorage(File(filePath))
        }

        fun build(): Noodle {
            return Noodle(storage, converter)
        }
    }
}

fun buildNoodle(options: Noodle.Builder.() -> Unit): Noodle = Noodle.Builder()
        .apply(options)
        .build()

