package com.noodle

import com.noodle.description.Description
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
}
