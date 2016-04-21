package com.noodle

import java.io.Serializable
import java.util.*

data class Record(val encodedKey: String, var data: ByteArray): Serializable {

  constructor(key: String, data: String): this(key, data.toByteArray())

  override fun equals(other: Any?): Boolean{
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as Record

    if (encodedKey != other.encodedKey) return false
    if (!Arrays.equals(data, other.data)) return false

    return true
  }

  override fun hashCode(): Int{
    var result = encodedKey.hashCode()
    result += 31 * result + Arrays.hashCode(data)
    return result
  }

}