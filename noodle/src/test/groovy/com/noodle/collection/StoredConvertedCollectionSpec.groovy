package com.noodle.collection

import com.google.gson.Gson
import com.noodle.converter.GsonConverter
import com.noodle.description.Description
import com.noodle.storage.FileMappedBufferStorage
import com.noodle.util.Data
import org.robospock.RoboSpecification

/**
 *
 */
class StoredConvertedCollectionSpec extends RoboSpecification {

  private Description<Data> description
  private GsonConverter converter
  private FileMappedBufferStorage storage
  private StoredConvertedCollection<Data> collection
  private File file

  void setup() {
    description = Description.of(Data)
        .withIdField("id")
        .build()

    converter = new GsonConverter(new Gson())
    file = new File("collection-test.noodle")
    storage = new FileMappedBufferStorage(file)
    collection = new StoredConvertedCollection<Data>(Data,
        description,
        converter,
        storage)

  }

  void cleanup() {
    file.delete()
  }

  def "add and get some item"() {
    given:
    def data = new Data(name: "Oliver")

    when:
    def saved = collection.put(data).now()

    then:
    saved.id != 0

    and:
    data == collection.get(saved.id).now()

  }

  def "should get all items that were put previously"() {
    given:
    def items = [new Data(name: "Hello"), new Data(name: ", "), new Data(name: " world"), new Data(name: "!")]

    when:
    items.each {collection.put(it).now()}

    then:
    items == collection.all().now()

  }
}
