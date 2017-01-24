package com.noodle;

import org.robolectric.annotation.Config
import org.robospock.RoboSpecification

/**
 *
 */
@Config(sdk = 21, manifest = "src/main/AndroidManifest.xml")
class InMemorySpec extends RoboSpecification {

  def "should pass"() {
    expect:
    1 == 1
  }
}
