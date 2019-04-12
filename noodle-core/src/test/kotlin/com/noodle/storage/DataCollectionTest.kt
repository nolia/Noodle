package com.noodle.storage

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class DataCollectionTest {

    private val collectionName = "test_collection"
    private val collectionNameBytes = collectionName.toByteArray()

    private val mockStorage: Storage = mockk(relaxUnitFun = true) {
        every { getKeysWithPrefix(collectionNameBytes) } returns emptyList()
    }

    private lateinit var dataCollection: DataCollection

    @Before
    fun setUp() {
        dataCollection = DataCollection(
                storage = mockStorage,
                name = collectionName
        )
    }

    @Test
    fun shouldStartWithNextId() {
        assert(dataCollection.nextId() == 1L)
    }

    @Test
    fun shouldFillUpIdSet() {
        // Given
        val storageIds = listOf(
                "$collectionName:1",
                "$collectionName:2",
                "$collectionName:3"
        ).map { it.toByteArray() }
        val storage = mockk<Storage> {
            every { getKeysWithPrefix(any()) } returns storageIds
        }

        // When
        dataCollection = DataCollection(storage, collectionName)

        // Then
        assert(dataCollection.size() == storageIds.size)
        assert(dataCollection.nextId() == 4L)
    }
}
