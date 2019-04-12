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

    // Test data.
    private val key = "key"
    private val value = "value"
    private val noodleKey = "$NOODLE_KEY_PREFIX:$key"
    private val keyBytes = noodleKey.toByteArray()
    private val record = Record(keyBytes, value.toByteArray())

    @Before
    fun setUp() {
        storage = mockk(relaxUnitFun = true) {
            every { get(keyBytes) } returns record
        }
        converter = mockk(relaxUnitFun = true) {
            every { toBytes(any<String>()) } answers { firstArg<String>().toByteArray() }
            every { fromBytes(any(), any<Class<String>>()) } answers { String(firstArg<ByteArray>()) }
        }

        noodle = buildNoodle {
            storage = this@NoodleTest.storage
            converter = this@NoodleTest.converter
        }

    }

    @Test
    fun shouldPut() {
        // When
        noodle.put(key, value)

        // Then
        verify { converter.toBytes(value) }
        verify { storage.put(record) }
    }

    @Test
    fun shouldGet() {
        // When
        val got: String? = noodle[key]

        // Then
        assert(got == value)
        verify { storage.get(keyBytes) }
        verify { converter.fromBytes(value.toByteArray(), String::class.java) }
    }

    @Test
    fun shouldDelete() {
        // Given
        every { storage.remove(keyBytes) } returns record

        // Expect
        assert(noodle.delete(key))
        verify { storage.remove(keyBytes) }
    }

    @Test
    fun useIndexedSetOperator() {
        // When
        noodle[key] = value

        // Expect
        verify { converter.toBytes(value) }
        verify { storage.put(record) }
    }
}
