package com.noodle.collection

interface Collection<T> : MutableIterable<T> {

    val size: Int

    fun getById(id: Long): T?

    fun setValue(id: Long, value: T): T

    fun deleteById(id: Long): T?

}

operator fun <T> Collection<T>.get(id: Long): T? = getById(id)

operator fun <T> Collection<T>.get(vararg ids: Long): List<T?> = ids.map { get(it) }

operator fun <T> Collection<T>.set(id: Long, value: T?) {
    if (value != null) {
        setValue(id, value)
    } else {
        deleteById(id)
    }
}

