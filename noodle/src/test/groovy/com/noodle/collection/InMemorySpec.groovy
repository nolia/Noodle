package com.noodle.collection

import com.noodle.description.Description
import com.noodle.util.Data
import org.robolectric.annotation.Config
import org.robospock.RoboSpecification

/**
 *
 */
@Config(sdk = 21, manifest = "src/main/AndroidManifest.xml")
class InMemorySpec extends RoboSpecification {

  private InMemoryCollection<Data> inMemoryCollection
  private Description<Data> stringDescription

  void setup() {
    stringDescription = Description.of(Data)
        .withGetIdOperator({ it.id } as Description.GetIdOperator<Data>)
        .withSetIdOperator({ data, id -> data.id = id } as Description.SetIdOperator<Data>)
        .build()

    inMemoryCollection = new InMemoryCollection<String>(Data, stringDescription)
  }

  def "should add item"() {
    given:
    def a = new Data()
    a.name = "A!"

    when:
    inMemoryCollection.put(a).now()

    then:
    inMemoryCollection.dataMap.containsValue(a)

  }
}
