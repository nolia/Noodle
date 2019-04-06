package com.noodle

import com.noodle.storage.Record
import com.noodle.storage.Storage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NoodleTest {

    private lateinit var storage: Storage
    private lateinit var converter: Converter

    private lateinit var noodle: Noodle

    @Before
    fun setUp() {
        storage = mockk(relaxUnitFun = true)
        converter = mockk(relaxUnitFun = true)

        noodle = buildNoodle {
            storage = this@NoodleTest.storage
            converter = this@NoodleTest.converter
        }
    }

    @Test
    fun shouldGet() {
        // Given
        val data = "data".toByteArray()
        val key = "key".toByteArray()
        val record = Record(key, data)

        every { converter.toBytes("key") } returns key
        every { converter.toBytes("value") } returns data

        // When
        noodle.put("key", "value")

        // Then
        verify { converter.toBytes("value") }
        verify { storage.put(record) }
    }
}
