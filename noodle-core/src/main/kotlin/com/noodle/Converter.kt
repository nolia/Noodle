package com.noodle

import com.google.gson.Gson
import java.nio.charset.Charset

interface Converter {

    fun <T> toBytes(value: T): ByteArray

    fun <T> fromBytes(bytes: ByteArray, clazz: Class<T>): T
}

class GsonConverter(private val gson: Gson) : Converter {

    private val defaultCharset = Charset.forName("UTF-8")

    override fun <T> toBytes(value: T): ByteArray =
            gson.toJson(value).toByteArray(defaultCharset)

    override fun <T> fromBytes(bytes: ByteArray, clazz: Class<T>): T =
            gson.fromJson<T>(String(bytes, defaultCharset), clazz.javaClass)
}
