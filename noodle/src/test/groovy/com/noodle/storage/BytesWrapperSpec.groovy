package com.noodle.storage

import org.robospock.RoboSpecification
import spock.lang.Unroll

class BytesWrapperSpec extends RoboSpecification {

  def "should create byte wrapper from bytes"() {
    given:
    def bytes = "12345abcdefg;987åß∂ƒœ".bytes

    when:
    def wrapper = new BytesWrapper(bytes)

    then:
    wrapper.bytes == bytes
  }

  def "should call equals properly"() {
    given:
    def thisWrapper = new BytesWrapper("123".bytes)
    def thatWrapper = new BytesWrapper("123".bytes)

    expect:
    thisWrapper.equals(thatWrapper)
  }

  def "should call hashCode properly"() {
    given:
    def bytes = "hello, world!".bytes
    def wrapper = new BytesWrapper(bytes)

    when:
    def hashCode = wrapper.hashCode()

    then:
    hashCode == Arrays.hashCode(bytes)
  }

  def "should call toString"() {
    given:
    def bytes = "what's up there?".bytes

    when:
    def wrapper = new BytesWrapper(bytes)

    then:
    new String(bytes) == wrapper.toString()
  }

  @Unroll
  def "should check hasPrefix #prefix"(String prefix, boolean result) {
    given:

    def wrapper = new BytesWrapper("123456".bytes)

    when:
    def actual = wrapper.hasPrefix(prefix.bytes)

    then:
    actual == result

    where:
    prefix      | result
    "a"         | false
    "b"         | false
    "abcd"      | false
    "abcdefghk" | false
    "12"        | true
    "123"       | true
    "1234"      | true
    "12345"     | true
    "123456"    | true
  }

  @Unroll
  def "should compare wrapper with #name"(name, s1, s2, result) {
    given:
    def first = new BytesWrapper(s1.bytes)
    def second = s2 != null ? new BytesWrapper(s2.bytes) : null

    when:
    def compare = first.compareTo second

    then:
    compare == result

    where:
    name               | s1   | s2   | result
    "equal"            | "0"  | "0"  | 0
    "null"             | "0"  | null | "0".bytes[0]
    "different letter" | "c"  | "a"  | 2
    "shorter"          | "02" | "0"  | 1
    "longer"           | "0"  | "01" | -1
  }
}