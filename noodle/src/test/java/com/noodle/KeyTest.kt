package com.noodle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * @author Nikolay Soroka - Stanfy (http://stanfy.com)
 */
class KeyTest: BaseTests() {

  @Test fun packUnpackIsOk() {
    val key = Key("object", 1, null)

    val str = key.pack().toString()
    println(str)
    val unpacked = Key.unpack(str)
    println(unpacked)

    assertEquals(key, unpacked)
  }

  @Test fun mustPackParentKey() {
    val parentKey = Key("parent", 1, null)
    val key = Key("child", 2, parentKey)

    val packed = key.pack().toString()
    println(packed)

    val unpacked = Key.unpack(packed)

    assertEquals(key, unpacked)
    assertNotNull(unpacked.parent)
    assertEquals(key.parent, unpacked.parent)
  }

  @Test fun mustEncodeAndDecodeKey() {
    val key = Key("object", 1, null)

    val encoded = key.encode()
    val decoded = Key.decode(encoded)

    assertEquals(key, decoded)
  }
}