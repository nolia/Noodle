package com.noodle.collection

import com.google.gson.Gson
import com.noodle.Description
import com.noodle.storage.Encryption
import com.noodle.storage.RandomAccessFileStorage
import com.noodle.storage.Storage
import com.noodle.util.Data
import org.robospock.RoboSpecification

class StoredConvertedCollectionSpec extends RoboSpecification {

  private Description<Data> description
  private GsonConverter converter
  private Storage storage
  private StoredConvertedCollection<Data> collection
  private File file

  void setup() {
    description = Description.of(Data)
        .withIdField("id")
        .build()

    converter = new GsonConverter(new Gson())
    file = new File("collection-test.noodle")
    storage = new RandomAccessFileStorage(file, Encryption.NO_ENCRYPTION)
    collection = new StoredConvertedCollection<Data>(Data,
        description,
        converter,
        storage)

  }

  void cleanup() {
    file.delete()
  }

  def "should add with put"() {
    given:
    def data = new Data(name: "Hello!")

    when:
    collection.putAsync(data).value()

    then:
    data.id != 0
  }

  def "should update item with put"() {
    given:
    def data = new Data(name: "Hello!")
    def putId = collection.putAsync(data).value().id

    when:
    data.name = "Other"
    collection.putAsync(data).value()

    then:
    collection.countAsync().value() == 1
    collection.getAsync(putId).value().name == "Other"
  }

  def "should add multiple items with putAll"() {
    given:
    def item1 = new Data(name: "item 1")
    def item2 = new Data(name: "item 2")
    def item3 = new Data(name: "item 3")

    when:
    collection.putAllAsync(item1, item2, item3).value()

    then:
    collection.countAsync().value() == 3

    and:
    item1.id != 0
    item2.id != 0
    item3.id != 0
  }

  def "should put items from iterable collection"() {
    given:
    def items = [
        new Data(name: "item1"),
        new Data(name: "item2"),
        new Data(name: "item3")
    ]

    when:
    collection.putAllAsync(items).value()

    then:
    collection.countAsync().value() == items.size()

    and:
    collection.getAllAsync().value().containsAll(items)
  }

  def "should return null when item not found"() {
    expect:
    collection.getAsync(345).value() == null
  }

  def "add and get some item"() {
    given:
    def data = new Data(name: "Oliver")

    when:
    def saved = collection.putAsync(data).value()

    then:
    saved.id != 0

    and:
    data == collection.getAsync(saved.id).value()
  }

  def "should get all items that were put previously"() {
    given:
    def items = [new Data(name: "Hello"), new Data(name: ", "), new Data(name: " world"), new Data(name: "!")]

    when:
    items.each { collection.putAsync(it).value() }

    then:
    def listOfData = collection.getAllAsync().value()
    listOfData.containsAll(items)
    listOfData.size() == items.size()
  }

  def "should delete item"() {
    given:
    def item = new Data(name: "You shall not pass!")
    collection.putAsync(item).value()

    when:
    def deleted = collection.deleteAsync(item.id).value()

    then:
    deleted == item
  }

  def "should delete all items"() {
    given:
    def items = [
        new Data(name: "Item 1"),
        new Data(name: "Item 2"),
        new Data(name: "Item 3")
    ]

    and:
    items.each { collection.putAsync(it).value() }

    expect:
    collection.clearAsync().value()

    and:
    collection.getAllAsync().value().isEmpty()
  }

  def "should count all items in a collection"(List items, int count) {
    given:
    items.each { collection.putAsync(it).value() }

    expect:
    collection.countAsync().value() == count

    where:
    items                    | count
    []                       | 0
    [new Data()]             | 1
    [new Data(), new Data()] | 2
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
      collection.putAsync(it).value()
    }

    when:
    def filtered = collection.filterAsync(
        { it.name.startsWith("ab") } as Collection.Predicate<Data>
    ).value()

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

    items.each { collection.putAsync(it).value() }

    when:
    def newCollection = new StoredConvertedCollection<Data>(Data,
        description,
        converter,
        storage)

    then:
    newCollection.sequenceId.get() == 5
  }
}
