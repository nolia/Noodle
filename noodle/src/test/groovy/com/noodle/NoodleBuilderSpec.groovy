package com.noodle

import android.content.Context
import com.noodle.converter.Converter
import com.noodle.description.Description
import com.noodle.encryption.Encryption
import com.noodle.util.AnnotatedData
import com.noodle.util.Data
import com.noodle.util.DoubleIdData
import org.robolectric.RuntimeEnvironment
import org.robospock.RoboSpecification

/**
 *
 */
class NoodleBuilderSpec extends RoboSpecification {

  private Context context

  void setup() {
    context = RuntimeEnvironment.application
  }

  def "should create Noodle with builder"() {
    given:
    Converter converter = Mock()
    def path = "build/tmp/other.noodle"
    def description = Description.of(Data).withIdField("id").build()
    def encryption = Mock(Encryption)

    when:
    def newNoodle = Noodle.with(context)
        .converter(converter)
        .filePath(path)
        .addType(description)
        .encryption(encryption)
        .build()

    then:
    newNoodle.converter == converter

    newNoodle.descriptionHashMap.containsValue(description)
  }

  def "should register annotated type with builder"() {
    when:
    def newNoodle = Noodle.with(context)
        .addType(AnnotatedData)
        .build()

    then:
    newNoodle.descriptionHashMap.containsKey(AnnotatedData.class.getCanonicalName())
  }

  def "should not register class with more than one id"() {
    when:
    Noodle.with(context).addType(DoubleIdData)

    then:
    thrown RuntimeException
  }

  def "should not register class without Id annotation"() {
    when:
    Noodle.with(context).addType(Data)

    then:
    thrown RuntimeException
  }

}
