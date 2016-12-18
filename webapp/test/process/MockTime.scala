package process

import java.util.concurrent.atomic.AtomicLong

/**
  * Thread safe time system
  * feasible for testing
  */
class MockTime extends Time {
  var time = new AtomicLong(0)

  override def currentMillis: Long = time.get()

  override def sleep(millis: Long): Unit = {
    time.addAndGet(millis)
  }
}