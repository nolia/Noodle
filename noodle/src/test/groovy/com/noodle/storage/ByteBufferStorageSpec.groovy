package com.noodle.storage

import org.robospock.RoboSpecification
import spock.lang.Unroll

/**
 *
 */
class ByteBufferStorageSpec extends RoboSpecification {

  private ByteBufferStorage storage

  static Record r1 = new Record("1".getBytes(), "a".getBytes()),
                r2 = new Record("2".getBytes(), "b".getBytes()),
                r3 = new Record("3".getBytes(), "c".getBytes())

  void setup() {
    storage = new ByteBufferStorage()
  }

  def "put should update internals"() {
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

  def "put should replace old item"() {
    given:
    def item = new Record("123".getBytes(), "incorrect data".getBytes())
    storage.put(item)

    when:
    item.data = "correct, this one".getBytes()

    and:
    storage.put(item)

    then:
    def newItem = storage.get(item.key)
    newItem == item

    storage.treeMapIndex.size() == 1
    storage.lastPosition == newItem.size()

  }

  def "remove one"() {
    given:
    def r = new Record("1".getBytes(), "a".getBytes())

    when:
    storage.put(r)

    and:
    storage.remove(r.key)

    then:
    storage.lastPosition == 0
    storage.buffer.position() == storage.lastPosition
    storage.treeMapIndex.size() == 0
  }

  @Unroll
  def "remove at #position"(String position, Record toRemove) {
    when:
    storage.put(r1)
    storage.put(r2)
    storage.put(r3)

    and:
    storage.remove(toRemove.key)

    then:
    storage.buffer.position() == storage.lastPosition
    storage.treeMapIndex.size() == 2

    where:
    position | toRemove
    "start"  | r1
    "middle" | r2
    "end"    | r3
  }


  def "get returns same item"() {
    given:
    def item = new Record("123".getBytes(), "data".getBytes())

    when:
    storage.put(item)

    and:
    def found = storage.get(item.key)

    then:
    found == item
  }
}
