package com.noodle.storage

import org.robospock.RoboSpecification

import static com.noodle.util.ThreadUtils.spawnThreads

/**
 *
 */
class StorageThreadingSpec extends RoboSpecification {

  private RandomAccessFileStorage storage
  private File file

  void setup() {
    file = new File("test-storage-thread.noodle")
    storage = new RandomAccessFileStorage(file, Encryption.NO_ENCRYPTION)
  }

  void cleanup() {
    storage = null
    file.delete()
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
    storage.index.size() == 1
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
    storage.index.size() == 0
  }


}
