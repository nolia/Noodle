package com.noodle

import org.robospock.RoboSpecification

import java.util.concurrent.Callable
import java.util.concurrent.Executor

/**
 *
 */
class CallSpec extends RoboSpecification {

  private Call<String> result
  private executor

  void setup() {
    result = new Call<String>({ "hello, world!" } as Callable<String>)
    executor = { it.run() } as Executor
  }

  def "should call action when now() is called"() {
    given:
    def mockAction = Mock(Callable)
    result = new Call<>(mockAction)

    when:
    result.value()

    then:
    1 * mockAction.call()
  }

  def "should return value from callable"() {
    given:
    def expected = "Hello!"
    def callable = {
      return expected
    } as Callable<String>

    when:
    def actual = new Call<>(callable).value()

    then:
    actual == expected
  }

  def "should re-throw runtime exceptions"() {
    given:
    def callable = {
      throw new UnsupportedOperationException("No way!")
    } as Callable

    when:
    new Call<>(callable).value()

    then:
    RuntimeException e = thrown()
    e.cause instanceof UnsupportedOperationException
  }

  def "should throw exception when executor is not specified"() {
    when:
    result.get()

    then:
    thrown RuntimeException
  }

  def "should use specified executor"() {
    given:
    Executor mockExecutor = Mock()

    when:
    result.executeOn(mockExecutor).get()

    then:
    1 * mockExecutor.execute(_)
  }

  def "should notify callback on result"() {
    given:
    def callback = Mock(Call.Callback)

    when:
    result.executeOn(this.executor).withCallback(callback).get()

    then:
    1 * callback.onReady("hello, world!")
    0 * callback.onError(_)
  }

  def "should notify callback on exception"() {
    given:
    def callback = Mock(Call.Callback)
    result = new Call<>({ throw UnsupportedOperationException("Ha!") } as Callable)

    when:
    result.executeOn(this.executor).withCallback(callback).get()

    then:
    0 * callback.onReady(_)
    1 * callback.onError(_ as RuntimeException)
  }

  def "should convert to rx Observable"() {
    given:
    def mockAction = Mock(Callable)
    result = new Call<>(mockAction)

    when:
    def received = null
    boolean completed = false
    result.toRxObservable().subscribe(
        { received = it },
        { throw it },
        { completed = true }
    )

    then:
    1 * mockAction.call() >> "Hello!"
    received == "Hello!"
    completed
  }

  def "should notify observable with exception"() {
    given:
    def callable = {
      throw new UnsupportedOperationException("No way!")
    } as Callable

    when:
    Throwable error
    new Call<>(callable)
        .toRxObservable()
        .subscribe({}, { error = it }, {})

    then:
    error != null
  }
}
