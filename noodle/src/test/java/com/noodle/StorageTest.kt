package com.noodle

import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.util.*

/**
 * @author Nikolay Soroka - Stanfy (http://stanfy.com)
 */
class StorageTest : BaseTests() {

  private val files: ArrayList<File> = ArrayList()
  private var storages: ArrayList<Storage> = ArrayList()

  @After fun tearDown() {
    storages.forEach {
      it.close()
      it.file.delete()
    }

    files.forEach {
      println("Deleting file: " + it.absolutePath)
      it.delete()
    }

  }

  @Test fun createFile() {
    val st = newStorage()

    assert(st.file.exists())
  }

  @Test fun writeSomeData() {
    val st = Storage("text.noodle")
    val record = Record("key123", "data".toByteArray())
    st.put(record)
    st.close()

    storages.add(st)

    var newStorage = Storage("text.noodle")
    val read = newStorage.get("key123")

    assert(record.equals(read))
    st.close()
  }

  @Test fun readFromEmptyFile() {
    val st = newStorage()

    val r = st.get("123")

    assertNull(r)
  }

  @Test fun putAndDelete() {
    val st = newStorage()

    val record = Record("123", "data")
    st.put(record)

    st.delete("123")

    val r = st.get("123")
    assertNull(r)
  }

  @Test fun putMultiAndDeleteFromTheMiddle() {
    val st = newStorage()
    val r1 = Record("1", "a")
    val r2 = Record("2", "b")
    val r3 = Record("3", "c")

    st.put(r1)
    st.put(r2)
    st.put(r3)

    assert(st.delete(r2.encodedKey))

    assertNotNull(st.get(r1.encodedKey))
    assertNotNull(st.get(r3.encodedKey))

    assertNull(st.get(r2.encodedKey))
  }

  private fun newStorage(): Storage {
    val st = Storage("data.noodle")
    storages.add(st)
    return st
  }

}