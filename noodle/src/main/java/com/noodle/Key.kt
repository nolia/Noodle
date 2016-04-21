package com.noodle

import android.text.TextUtils
import android.util.Base64
import java.io.Serializable

data class Key(val kind: String, var intId: Int, var parent: Key?) : Comparable<Key>, Serializable {

  companion object {

    internal const val DELIMITER = '\n'

    fun decode(bytes: ByteArray): Key? {
      val decoded = String(Base64.decode(bytes, Base64.DEFAULT))

      return unpack(decoded)
    }

    internal fun unpack(decoded: String): Key {
      val components = decoded.split(DELIMITER)

      if (components.size < 3) {
        throw IllegalArgumentException("Invalid input base64 string")
      }

      val kind = components[0]
      val id = components[1]
      val parentStart = components[2]
      var parent: Key? = null

      if (!TextUtils.isEmpty(parentStart)) {
        val parentContent = decoded.substring(decoded.indexOf(parentStart))

        parent = unpack(parentContent)
      }


      return Key(kind, Integer.parseInt(id), parent)
    }
  }

  override fun compareTo(other: Key): Int = this.hashCode().compareTo(other.hashCode())

  fun encode(): ByteArray {
    val str = pack().toString()
    return Base64.encode(str.toByteArray(), Base64.DEFAULT)
  }

  internal fun pack(): StringBuilder {
    var builder = StringBuilder()
    builder.append(kind).append(DELIMITER)
    builder.append(intId).append(DELIMITER)
    if (parent != null) {
      builder.append((parent as Key).pack())
    }

    return builder
  }
}
