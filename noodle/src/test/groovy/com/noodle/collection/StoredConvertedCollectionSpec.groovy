package com.noodle.collection

import com.google.gson.Gson
import com.noodle.converter.GsonConverter
import com.noodle.description.Description
import com.noodle.storage.FileMappedBufferStorage
import com.noodle.util.Data
import org.robospock.RoboSpecification

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

  def "should return null when item not found"() {
    expect:
    collection.get(345).now() == null
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

  def "should delete item"() {
    given:
    def item = new Data(name: "You shall not pass!")
    collection.put(item).now()

    when:
    def deleted = collection.delete(item.id).now()

    then:
    deleted == item
  }

  def "should filter items"() {
    given:
    def items = [
        new Data(name: "ab"),
        new Data(name: "abc"),
        new Data(name: "abc1"),
        new Data(name: "defg")
    ]

    and:
    items.each {
      collection.put(it).now()
    }

    when:
    def filtered = collection.filter(
        {it.name.startsWith("ab")} as Collection.Predicate<Data>
    ).now()

    then:
    filtered.size() == 3
    for (int i = 0; i < 3; i++) {
      filtered.contains(items.get(i))
    }
  }

  def "should pick up last stored id"() {
    given:
    def items = []
    5.times {
      items << new Data(name: "a" * it)
    }

    items.each {collection.put(it).now()}

    when:
    def newCollection = new StoredConvertedCollection<Data>(Data,
        description,
        converter,
        storage)

    then:
    newCollection.sequenceId == 5
  }
}
