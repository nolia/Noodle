package com.noodle

import com.noodle.description.Description
import com.noodle.storage.Record
import com.noodle.storage.Storage
import com.noodle.util.Data
import org.robolectric.RuntimeEnvironment
import org.robospock.RoboSpecification

/**
 *
 */
class NoodleSpec extends RoboSpecification {

  private Noodle noodle

  void setup() {
    noodle = new Noodle(RuntimeEnvironment.application)
  }

  def "should register type"() {
    given:
    def description = Description.of(Data).withIdField("id").build()

    when:
    noodle.registerType(Data, description)

    then:
    noodle.descriptionHashMap.containsKey(Data.class.getCanonicalName())
    noodle.descriptionHashMap.get(Data.class.getCanonicalName()) == description
  }

  def "should return typed collection"() {
    given:
    noodle.registerType(Data, Description.of(Data).withIdField("id").build())

    when:
    def dataCollection = noodle.collectionOf(Data)

    then:
    dataCollection != null
  }

  def "should throw RuntimeException when getting unregistered collection"() {
    when:
    noodle.collectionOf(Data)

    then:
    thrown RuntimeException
  }

  def "get collection for second time should return same collection"() {
    given:
    noodle.registerType(Data, Description.of(Data).withIdField("id").build())
    def collection = noodle.collectionOf(Data)

    when:
    def secondCollection = noodle.collectionOf(Data)

    then:
    secondCollection == collection
  }

  def "should put value by key"() {
    given:
    final def key = "key"
    final def value = "value"

    def mockStorage = Mock(Storage)
    noodle.storage = mockStorage

    when:
    noodle.put(key, value).now()

    then:
    1 * mockStorage.put({ it.key == noodle.keyValueKey(key) && it.data == noodle.converter.toBytes(value) })
  }

  def "should get value by key"() {
    setup:
    final def key = "key"
    final def expected = "expected"
    final def record = new Record(key.getBytes(), noodle.converter.toBytes(expected))

    def mockStorage = Mock(Storage)
    noodle.storage = mockStorage

    when:
    String value = noodle.get(key, String).now()

    then:
    1 * mockStorage.get(noodle.keyValueKey(key)) >> record
    value == expected
  }

  def "should return null if record is not in storage"() {
    given:
    final def notInTheStorage = "not here"

    def mockStorage = Mock(Storage)
    noodle.storage = mockStorage

    when:
    String notHere = noodle.get(notInTheStorage, String).now()

    then:
    1 * mockStorage.get(noodle.keyValueKey(notInTheStorage))
    notHere == null
  }

  def "should delete value by key"() {
    setup:
    final def key = "key"
    final def expected = "expected"
    final def record = new Record(key.getBytes(), noodle.converter.toBytes(expected))

    def mockStorage = Mock(Storage)
    noodle.storage = mockStorage

    when:
    Boolean deleted = noodle.delete(key).now()

    then:
    deleted
    1 * mockStorage.remove(noodle.keyValueKey(key)) >> record
  }

  def "should return false if not deleted"() {
    setup:
    final def key = "key"

    def mockStorage = Mock(Storage)
    noodle.storage = mockStorage

    when:
    Boolean deleted = noodle.delete(key).now()

    then:
    !deleted
    1 * mockStorage.remove(noodle.keyValueKey(key)) >> null
  }


}
