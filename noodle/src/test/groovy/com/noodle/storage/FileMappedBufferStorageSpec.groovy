package com.noodle.storage

import org.robospock.RoboSpecification

/**
 *
 */
class FileMappedBufferStorageSpec extends RoboSpecification {

  private File file
  private FileMappedBufferStorage storage

  static Record r1 = new Record("1".getBytes(), "a".getBytes()),
                r2 = new Record("2".getBytes(), "b".getBytes()),
                r3 = new Record("3".getBytes(), "c".getBytes())

  void setup() {
    file = new File("test.noodle")
    storage = new FileMappedBufferStorage(file)
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
    def anotherStorage = new FileMappedBufferStorage(file)

    then:
    anotherStorage.treeMapIndex.size() == records.size()
    records.each { r ->
      anotherStorage.treeMapIndex.containsKey(new BytesWrapper(r.key))

      anotherStorage.get(r.key) == r
    }

  }

  void cleanup() {
    storage = null
    file.delete()
  }
}
