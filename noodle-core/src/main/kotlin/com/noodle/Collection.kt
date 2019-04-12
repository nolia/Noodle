package com.noodle

interface Collection<T> : MutableIterable<T> {

    val size: Int

    fun getById(id: Long): T?

    fun getMultiple(ids: Iterable<Long>): List<T?> = ids.map { getById(it) }.toList()

    fun putValue(value: T): T

    fun putAll(values: Iterable<T>): List<T> = values.map { putValue(it) }.toList()

    fun setValue(id: Long, value: T): T

    fun deleteById(id: Long): T?

    fun deleteMultiple(ids: Iterable<Long>): List<T?> = ids.map { deleteById(it) }.toList()

}

class Description<T>(
        val clazz: Class<T>,
        val getId: (T) -> Long,
        val setId: (Long, T) -> T
)

operator fun <T> Collection<T>.get(id: Long): T? = getById(id)

operator fun <T> Collection<T>.get(vararg ids: Long): List<T?> = ids.map { get(it) }

operator fun <T> Collection<T>.set(id: Long, value: T?) {
    if (value != null) {
        setValue(id, value)
    } else {
        deleteById(id)
    }
}

