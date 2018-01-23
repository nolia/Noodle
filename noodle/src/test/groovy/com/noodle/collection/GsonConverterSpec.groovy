package com.noodle.collection

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.noodle.collection.GsonConverter
import org.robospock.RoboSpecification

/**
 *
 */
class GsonConverterSpec extends RoboSpecification {

  private GsonConverter converter
  private Gson gson

  void setup() {
    gson = new GsonBuilder().create()
    converter = new GsonConverter(gson)
  }

  def "string to bytes"() {
    given:
    def s = "Hello, world!"

    expect:
    gson.toJson(s).bytes == converter.toBytes(s)
  }

  def "string from bytes"() {
    given:
    def s = "Hey there!"
    def bytes = gson.toJson(s).bytes

    expect:
    s == converter.fromBytes(bytes, String)

  }
}
