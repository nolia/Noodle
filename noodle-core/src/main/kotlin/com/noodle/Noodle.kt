package com.noodle

import com.google.gson.Gson
import com.noodle.storage.RafStorage
import com.noodle.storage.Record
import com.noodle.storage.Storage
import java.io.File

class Noodle internal constructor(
        private val storage: Storage,
        private val converter: Converter
) {

    public constructor(filePath: String) : this(
            storage = RafStorage(File(filePath)),
            converter = GsonConverter(Gson())
    )

    fun <T> get(key: String): T {
        TODO("Not implemented")
    }

    fun <T> put(key: String, value: T) {
        val record = Record(
                key = converter.toBytes(key),
                data = converter.toBytes(value)
        )
        storage.put(record)
    }

    fun delete(key: String): Boolean {
        TODO("Not implemented")
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

fun buildNoodle(options: Noodle.Builder.() -> Unit): Noodle =
        Noodle.Builder()
                .apply(options)
                .build()

