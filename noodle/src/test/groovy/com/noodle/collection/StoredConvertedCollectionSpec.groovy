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

  void setup() {
    description = Description.of(Data)
        .withIdField("id")
        .build()

    converter = new GsonConverter(new Gson())
    storage = new FileMappedBufferStorage(new File("collection-test.noodle"))
    collection = new StoredConvertedCollection<Data>(Data,
        description,
        converter,
        storage)

  }

  def "add and get some item"() {
    given:
    def data = new Data(name: "Oliver")

    when:
    def saved = collection.put(data).now()

    then:
    saved.id != 0

    and:
    collection.get(saved.id).now() == data

  }
}
