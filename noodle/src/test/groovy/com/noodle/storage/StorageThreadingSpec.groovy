package com.noodle.storage

import org.robospock.RoboSpecification

import static com.noodle.util.ThreadUtils.spawnThreads

/**
 *
 */
class StorageThreadingSpec extends RoboSpecification {

  private ByteBufferStorage storage

  static Record r1 = new Record("1".getBytes(), "a".getBytes()),
                r2 = new Record("2".getBytes(), "b".getBytes()),
                r3 = new Record("3".getBytes(), "c".getBytes())

  void setup() {
    storage = new ByteBufferStorage()
  }

  def "should sync on getting and writing item with same key"() {
    given:
    final def recordKey = "123".getBytes()

    when:
    spawnThreads(10) { n ->
      if (storage.get(recordKey) == null) {
        storage.put(new Record(recordKey, "From thread: $n".getBytes()))
      }
    }.await()

    then:
    print "\nData: ${storage.get(recordKey)}"
    storage.treeMapIndex.size() == 1
  }

  def "should sync on removing one record"() {
    given:
    final def recordKey = "123".getBytes()
    storage.put(new Record(recordKey, "I'm here!".getBytes()))

    when:
    spawnThreads(10) { n ->
      if (storage.remove(recordKey) != null) {
        println()
        println("Removed in thread: $n")
      }
    }.await()

    then:
    storage.treeMapIndex.size() == 0
  }


}
