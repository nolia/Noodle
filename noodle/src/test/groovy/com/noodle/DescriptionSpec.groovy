package com.noodle

import com.noodle.util.Data
import org.robospock.RoboSpecification
import spock.lang.Shared

class DescriptionSpec extends RoboSpecification {

  private Data data

  @Shared
  Description.GetIdOperator defaultGetId = { t -> t.id } as Description.GetIdOperator

  @Shared
  Description.SetIdOperator defaultSetId = { t, id -> t.id = id } as Description.SetIdOperator

  void setup() {
    data = new Data(name: 'Data!')
  }

  def "should use get id operator"() {
    given:
    def getIdOperator = Mock(Description.GetIdOperator)
    def description = Description.of(Data)
        .withGetIdOperator(getIdOperator)
        .withSetIdOperator(defaultSetId)
        .build()

    when:
    description.idOfItem(data)

    then:
    1 * getIdOperator.getId(data)
  }

  def "should use set id operator"() {
    given:
    def setIdOperator = Mock(Description.SetIdOperator)
    def description = Description.of(Data)
        .withGetIdOperator(defaultGetId)
        .withSetIdOperator(setIdOperator).build()

    when:
    description.setItemId(data, 1)

    then:
    1 * setIdOperator.setId(data, 1)
  }

  def "should use reflection id operator"() {
    when:
    def description = Description.of(Data).withIdField("id").build()

    then:
    description.setIdOperator instanceof Description.ReflectionIdField
    description.setIdOperator instanceof Description.ReflectionIdField
    description.setIdOperator == description.getIdOperator
  }

  def "should throw an exception when using invalid id field"() {
    when:
    Description.of(Data).withIdField("invalidId").build()

    then:
    def e = thrown(RuntimeException)
    e.cause instanceof NoSuchFieldException
  }

  def "should check if field is final"() {
    when:
    Description.of(Data).withIdField("finalId").build()

    then:
    thrown RuntimeException
  }

  def "should check if id field is long or Long"() {
    when:
    Description.of(Data).withIdField("name").build()

    then:
    thrown RuntimeException
  }

  def "should handle building with or without id operators"(getId, setId) {
    when:
    Description.of(Data)
        .withGetIdOperator(getId)
        .withSetIdOperator(setId)
        .build()

    then:
    thrown RuntimeException

    where:
    getId        | setId
    null         | defaultSetId
    defaultGetId | null
    null         | null

  }
}
