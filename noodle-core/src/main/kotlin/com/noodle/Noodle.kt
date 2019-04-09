package com.noodle

import com.noodle.storage.RafStorage
import com.noodle.storage.Record
import com.noodle.storage.Storage
import java.io.File

class Noodle internal constructor(
        private val storage: Storage,
        private val converter: Converter
) {

    fun <T> get(key: String, clazz: Class<T>): T? {
        val record = storage.get(converter.toBytes(key)) ?: return null
        return converter.fromBytes(record.data, clazz)
    }

    inline fun <reified T> get(key: String): T? = get(key, T::class.java)

    fun <T> put(key: String, value: T) {
        val record = Record(
                key = converter.toBytes(key),
                data = converter.toBytes(value)
        )
        storage.put(record)
    }

    fun delete(key: String): Boolean = storage.remove(converter.toBytes(key)) != null

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

