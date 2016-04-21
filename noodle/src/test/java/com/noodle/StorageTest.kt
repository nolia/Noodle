package com.noodle

import org.junit.After
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.util.*

/**
 * @author Nikolay Soroka - Stanfy (http://stanfy.com)
 */
class StorageTest : BaseTests() {

  private val files: ArrayList<File> = ArrayList()
  private var storage: Storage? = null

  @After
  fun tearDown() {
    storage?.close()

    files.forEach {
      println("Deleting file: " + it.absolutePath)
      it.delete()
    }

  }

  @Test
  fun testCreateFile() {
    val name = "simple.noodle"
    storage = Storage(name)

    files += storage?.file!!
    assert(storage?.file!!.exists())
  }

  @Test
  fun testWriteSomeData() {
    val st = Storage("text.noodle")
    val record = Record("key123", "data".toByteArray())
    st.put(record)
    st.close()

    files += st.file

    var newStorage = Storage("text.noodle")
    val read = newStorage.get("key123")

    assert(record.equals(read))
    st.close()
  }

  @Test
  fun testReadFromEmptyFile() {
    val st = Storage("somefile.noodle")
    storage = st

    files += st.file

    val r = st.get("123")

    Assert.assertNull(r)
  }
}