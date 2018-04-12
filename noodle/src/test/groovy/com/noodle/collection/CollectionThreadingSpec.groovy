package com.noodle.collection

import com.google.gson.Gson
import com.noodle.Description
import com.noodle.storage.Encryption
import com.noodle.storage.RandomAccessFileStorage
import com.noodle.util.Data
import org.robospock.RoboSpecification

import java.util.concurrent.TimeUnit

import static com.noodle.util.ThreadUtils.spawnThreads

class CollectionThreadingSpec extends RoboSpecification {

  private Description<Data> description
  private GsonConverter converter
  private RandomAccessFileStorage storage
  private StoredConvertedCollection<Data> collection
  private File file

  void setup() {
    description = Description.of(Data)
        .withIdField("id")
        .build()

    converter = new GsonConverter(new Gson())
    file = new File("thread-test.noodle")
    storage = new RandomAccessFileStorage(file, Encryption.NO_ENCRYPTION)
    collection = new StoredConvertedCollection<Data>(Data,
        description,
        converter,
        storage)
  }

  void cleanup() {
    file.delete()
  }

  def "write new records in 10 threads at a time"() {
    when:
    spawnThreads(10) { number ->
      collection.putAsync(new Data(name: number)).value()
    }.await(2, TimeUnit.SECONDS)

    then:
    println '\nResults:'
    collection.getAllAsync().value().sort({ a, b -> a.id.compareTo(b.id) }).each {
      print "$it \n"
    }
  }

  def "should handle edit one item from 10 threads"() {
    given:
    def id = collection.putAsync(new Data(name: "")).value().id

    when:
    spawnThreads(10) { Integer number ->
      def data = collection.getAsync(id).value()
      data.name += number.toString()
      collection.putAsync(data).value()
    }.await(2, TimeUnit.SECONDS)

    then:
    println("\nResult")
    println collection.getAsync(id).value()
  }

  def "should sync on adding item"() {
    when:
    spawnThreads(10) { number ->
      def data = collection.getAsync(123).value()
      if (data == null) {
        data = new Data(id: 123, name: "From thread: $number")
        collection.putAsync(data).value()
      }

    }.await(2, TimeUnit.SECONDS)

    then:
    println "\ndata = ${collection.getAsync(123).value()}"
    collection.getAllAsync().value().size() == 1

  }


}
