package com.noodle.storage

import com.noodle.Collection
import com.noodle.Converter
import com.noodle.Description

private const val NO_ID = 0L

class ConvertedCollection<T>(
        private val dataCollection: DataCollection,
        private val converter: Converter,
        private val description: Description<T>
) : Collection<T> {

    override val size: Int
        get() = dataCollection.size()

    override fun getById(id: Long): T? {
        return converter.fromBytes(
                dataCollection.get(id) ?: return null,
                description.clazz
        )
    }

    override fun putValue(value: T): T = setValue(description.getId(value), value)

    override fun setValue(id: Long, value: T): T {
        var itemId = id
        if (itemId == NO_ID) itemId = dataCollection.nextId()

        val item = description.setId(itemId, value)

        dataCollection.put(itemId, converter.toBytes(item))

        return item
    }

    override fun deleteById(id: Long): T? {
        val bytes = dataCollection.delete(id) ?: return null
        return converter.fromBytes(bytes, description.clazz)
    }

    override fun iterator(): MutableIterator<T> = ConvertedIterator(dataCollection.dataIterator())

    private inner class ConvertedIterator(
            private val dataIterator: DataCollection.DataIterator
    ) : MutableIterator<T> {

        override fun hasNext(): Boolean = dataIterator.hasNext()

        override fun next(): T = converter.fromBytes(
                dataIterator.next(),
                description.clazz
        )

        override fun remove() = dataIterator.remove()
    }
}
