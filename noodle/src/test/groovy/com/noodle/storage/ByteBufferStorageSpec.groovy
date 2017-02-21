package com.noodle.storage

import org.robospock.RoboSpecification
import spock.lang.Unroll

/**
 *
 */
class ByteBufferStorageSpec extends RoboSpecification {

  private ByteBufferStorage storage
  private Random random = new Random(1134)

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

  def "put with same bytes should get on same place"() {
    given:
    def item = new Record("123".getBytes(), "abc".getBytes())
    storage.put(item)
    def wrapper = new BytesWrapper(item.key)
    def pos = storage.treeMapIndex.get(wrapper)

    // Put other arbitrary items.
    [r1, r2, r3].each {storage.put(it)}

    when:
    item.data = "cba".getBytes()

    and:
    storage.put(item)

    then:
    storage.treeMapIndex.get(wrapper) == pos
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
    [r1, r2, r3].each {storage.put(it)}

    and:
    storage.remove(toRemove.key)

    then:
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

  def "remove unknown item should return null"() {
    expect:
    storage.remove(r1.key) == null
  }

  def "should iterate with prefixedWith method"() {
    given:
    def items = [
        new Record("abaa".getBytes(), "1".getBytes()),
        new Record("abba".getBytes(), "2".getBytes()),
        new Record("abbc".getBytes(), "3".getBytes()),
        new Record("abbb".getBytes(), "4".getBytes()),
        new Record("ddde".getBytes(), "5".getBytes())
    ]
    items.each {storage.put(it)}

    when:
    def list = storage.prefixedWith("ab".getBytes())

    then:
    list.size() == 4
    list.containsAll items.subList(0, 4)*.key
  }

  def "new record should grow buffer on double size of record"() {
    when:
    byte[] data = new byte[128]
    random.nextBytes(data)

    def record = new Record("1".bytes, data)
    storage.put(record)

    then:
    storage.buffer.limit() == ByteBufferStorage.INITIAL_SIZE + 2 * record.size()
  }

  def "add more records should grow the buffer"() {
    when:
    // 10 Kb of data.
    10.times {
      def key = it.toString().getBytes()
      byte[] data = new byte[1024]
      random.nextBytes(data)
      storage.put(new Record(key, data))
    }

    then:
    storage.buffer.limit() > 10 * 1024
  }

  def "should put and get 1000 items"() {
    given:
    def records = []
    1000.times {
      def key = it.toString().getBytes()
      byte[] data = new byte[128]
      random.nextBytes(data)
      records << new Record(key, data)
    }

    when:
    records.each {
      storage.put(it)
    }

    then:
    records.each { Record r ->
      storage.get(r.key) == r
    }
  }

  @Unroll
  def "should safely delete elements of random length at #position"(String position, int toRemove) {
    given:
    def arr = new ArrayList<Record>()
    3.times {
      def key = new byte[random.nextInt(5) + 10 * it + 1]
      random.nextBytes(key)
      key[0] = it.byteValue()

      def data = new byte[random.nextInt(10) + 20 * it + 1]
      random.nextBytes(data)


      def record = new Record(key, data)
      arr << record

      storage.put(record)
    }

    def removedKey = arr[toRemove].key

    when:
    storage.remove(removedKey)

    then:
    3.times { i ->
      def key = arr[i].key
      if (key == removedKey) {
        storage.get(key) == null
      } else {
        storage.get(key) == arr[i]
      }
    }


    where:
    position | toRemove
    "start"  | 0
    "middle" | 1
    "end"    | 2
  }
}
