package com.noodle.storage

import org.robospock.RoboSpecification

/**
 *
 */
class ByteBufferStorageSpec extends RoboSpecification {

  private ByteBufferStorage storage

  void setup() {
    storage = new ByteBufferStorage()
  }

  def "put and get"() {
    given:
    def key = "123".getBytes()
    def data = "abc".getBytes();
    def record = new Record(key, data)

    when:
    storage.put(record)

    then:
    storage.lastPosition == record.size()
    storage.buffer.position() == storage.lastPosition
    storage.treeMapIndex.size() == 1
    storage.treeMapIndex.containsKey(new BytesWrapper(key))
    storage.treeMapIndex.get(new BytesWrapper(key)) == 0
  }

  def "Remove"() {

  }

  def "Get"() {

  }
}
