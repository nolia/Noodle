package com.noodle

interface Converter {

  fun <T> toBytes(t: T): ByteArray

  fun <T> fromBytes(bytes: ByteArray): T?

}