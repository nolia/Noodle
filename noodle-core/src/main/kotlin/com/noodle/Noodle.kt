package com.noodle

import com.noodle.storage.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal const val NOODLE_KEY_PREFIX = "\$noodle\$"

class Noodle internal constructor(
        private val storage: Storage,
        private val converter: Converter
) {

    private val dataCollections = ConcurrentHashMap<String, DataCollection>()

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

    //region Collections

    fun <T> getCollection(name: String, description: Description<T>): Collection<T> {
        val dataCollection = dataCollections[name] ?: DataCollection(storage, name).also { dataCollections[name] = it }
        return ConvertedCollection(dataCollection, converter, description)
    }

    inline fun <reified T> collectionOf(
            name: String,
            noinline getId: (T) -> Long,
            noinline setId: (Long, T) -> T
    ): Collection<T> = getCollection(
            name,
            Description(T::class.java, getId, setId)
    )

    //endregion

    private fun recordKey(key: String): ByteArray = "$NOODLE_KEY_PREFIX:$key".toByteArray()

    class Builder {
        lateinit var storage: Storage
        lateinit var converter: Converter

        fun setFilePath(filePath: String) {
            storage = RafStorage(File(filePath))
        }

        fun build(): Noodle = Noodle(storage, converter)
    }
}

fun buildNoodle(options: Noodle.Builder.() -> Unit): Noodle = Noodle.Builder()
        .apply(options)
        .build()

