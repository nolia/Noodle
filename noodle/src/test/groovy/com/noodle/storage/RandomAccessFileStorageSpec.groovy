package com.noodle.storage

import com.noodle.encryption.NoEncryption
import org.robospock.RoboSpecification
import spock.lang.Unroll;

class RandomAccessFileStorageSpec extends RoboSpecification {

  private Random random = new Random(123)

  private File file
  private RandomAccessFileStorage storage

  static Record r1 = new Record("1".getBytes(), "a".getBytes()),
                r2 = new Record("2".getBytes(), "b".getBytes()),
                r3 = new Record("3".getBytes(), "c".getBytes())

  void setup() {
    file = new File("test.noodle")
    storage = new RandomAccessFileStorage(file, new NoEncryption())
  }

  void cleanup() {
    storage = null
    file.delete()
  }

  def "opens empty file"() {
    expect:
    storage != null
  }

  def "put and get should work"() {
    given:
    def r = new Record("1".getBytes(), "a".getBytes())

    when:
    storage.put(r)

    then:
    storage.get(r.key) == r
  }

  def "should remap indexes"() {
    given:
    def records = [r1, r2, r3]

    when:
    records.each {
      storage.put it
    }

    and:
    def anotherStorage = new RandomAccessFileStorage(file, new NoEncryption())

    then:
    anotherStorage.index.size() == records.size()
    records.each { r ->
      anotherStorage.index.containsKey(new BytesWrapper(r.key))

      anotherStorage.get(r.key) == r
    }
  }

  def "should grow file when adding records"() {
    given:
    int totalSize = 0
    def records = []
    1000.times {
      def key = it.toString().getBytes()
      byte[] data = new byte[128]
      random.nextBytes(data)
      def r = new Record(key, data)
      totalSize += r.size()
      records << r
    }

    when:
    records.each {
      storage.put(it)
    }

    then:
    file.size() == totalSize
  }

  @Unroll
  def "should shrink file when deleting items at #part"(int start, String part) {
    given:
    int totalSize = 0
    def records = []
    1000.times {
      def key = String.format("%d", it).bytes
      byte[] data = new byte[128]
      random.nextBytes(data)
      def r = new Record(key, data)
      records << r
      totalSize += r.size()

      storage.put(r)
    }

    when:
    int removedSize = 0
    for (int i = start; i < start + 500; i++) {
      storage.remove(records[i].key)
      removedSize += records[i].size()
    }

    then:
    file.size() == totalSize - removedSize

    where:
    start | part
    0     | "beginning"
    250   | "middle"
    500   | "end"
  }
}