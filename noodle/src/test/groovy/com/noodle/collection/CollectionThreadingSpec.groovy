package com.noodle.collection

import com.google.gson.Gson
import com.noodle.converter.GsonConverter
import com.noodle.description.Description
import com.noodle.encryption.NoEncryption
import com.noodle.storage.RandomAccessFileStorage
import com.noodle.storage.Storage
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
    storage = new RandomAccessFileStorage(file, new NoEncryption())
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
      collection.put(new Data(name: number)).now()
    }.await(2, TimeUnit.SECONDS)

    then:
    println '\nResults:'
    collection.all().now().sort({ a, b -> a.id.compareTo(b.id) }).each {
      print "$it \n"
    }
  }

  def "should handle edit one item from 10 threads"() {
    given:
    def id = collection.put(new Data(name: "")).now().id

    when:
    spawnThreads(10) { Integer number ->
      def data = collection.get(id).now()
      data.name += number.toString()
      collection.put(data).now()
    }.await(2, TimeUnit.SECONDS)

    then:
    println("\nResult")
    println collection.get(id).now()
  }

  def "should sync on adding item"() {
    when:
    spawnThreads(10) { number ->
      def data = collection.get(123).now()
      if (data == null) {
        data = new Data(id: 123, name: "From thread: $number")
        collection.put(data).now()
      }

    }.await(2, TimeUnit.SECONDS)

    then:
    println "\ndata = ${collection.get(123).now()}"
    collection.all().now().size() == 1

  }


}
