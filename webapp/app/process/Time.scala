package process

trait Time {
  def currentMillis: Long

  def sleep(millis: Long)
}

object Time {
  def default = new Time {
    override def currentMillis: Long = System.currentTimeMillis()

    override def sleep(millis: Long): Unit = Thread.sleep(millis)
  }
}