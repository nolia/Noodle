package com.noodle.description

import com.noodle.util.Data
import org.robospock.RoboSpecification

class DescriptionSpec extends RoboSpecification {

  private Data data

  void setup() {
    data = new Data(name: 'Data!')
  }

  def "should use get id operator"() {
    given:
    def getIdOperator = Mock(Description.GetIdOperator)
    def description = Description.of(Data).withGetIdOperator(getIdOperator).build()

    when:
    description.idOfItem(data)

    then:
    1 * getIdOperator.getId(data)
  }

  def "should use set id operator"() {
    given:
    def setIdOperator = Mock(Description.SetIdOperator)
    def description = Description.of(Data)
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
}
