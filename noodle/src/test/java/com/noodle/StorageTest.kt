package com.noodle

import org.junit.After
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
    val st = Storage("simple.noodle")
    storages.add(st)

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
    val st = Storage("somefile.noodle")
    storages.add(st)

    val r = st.get("123")

    assertNull(r)
  }

  @Test fun putAndDelete() {
    val st = Storage("somefile.noodle")
    storages.add(st)

    val record = Record("123", "data")
    st.put(record)

    st.delete("123")

    val r = st.get("123")
    assertNull(r)
  }

}